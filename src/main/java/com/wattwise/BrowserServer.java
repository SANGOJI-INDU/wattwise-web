
package com.wattwise;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.wattwise.database.DatabaseConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;

public class BrowserServer {

    private static final String PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>WattWise | Smart Energy Estimator</title>
<style>
*{box-sizing:border-box}
body{margin:0;font-family:Arial,sans-serif;background:#f3f6fb;color:#172033}
header{background:#123b68;color:white;padding:25px 7%;display:flex;justify-content:space-between;align-items:center}
header h1{margin:0;font-size:27px}
header p{margin:6px 0 0;color:#d8e7f7}
main{max-width:1100px;margin:28px auto;padding:0 18px}
.card{background:white;padding:22px;border-radius:13px;margin-bottom:20px;box-shadow:0 3px 14px #17203312}
h2{margin-top:0;color:#173e68;font-size:21px}
.appliance{display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:12px;align-items:end;margin:12px 0}
.field small{display:block;font-weight:bold;font-size:11px;color:#62718a;margin-bottom:7px}
input,select{width:100%;padding:12px;border:1px solid #cbd5e1;border-radius:7px;font-size:14px;background:white}
button{border:0;border-radius:7px;padding:12px 17px;background:#1769aa;color:white;font-weight:bold;cursor:pointer}
button:hover{opacity:.9}
button.secondary{background:#e6eef7;color:#173e68}
button.danger{background:#fce8e8;color:#a32626;padding:10px}
.actions{display:flex;gap:10px;flex-wrap:wrap;margin-top:16px}
.settings{display:grid;grid-template-columns:repeat(3,1fr);gap:15px}
.results{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}
.result{background:#edf5fc;padding:18px;border-radius:10px}
.result small{color:#53677f}
.result strong{display:block;font-size:24px;margin-top:9px;color:#164d7b}
.status{padding:13px;border-radius:8px;margin-top:14px;background:#edf5fc}
.bar-row{display:grid;grid-template-columns:140px 1fr 80px;gap:10px;align-items:center;margin:12px 0;font-size:13px}
.bar{height:13px;background:#e5edf5;border-radius:20px;overflow:hidden}
.bar span{display:block;height:100%;background:#2384c6;border-radius:20px}
table{width:100%;border-collapse:collapse;font-size:14px}
th,td{text-align:left;padding:11px;border-bottom:1px solid #e5eaf0}
th{background:#f1f5f9}
.muted{color:#68788c;font-size:13px}
@media(max-width:700px){
 header{display:block}.appliance{grid-template-columns:1fr 1fr}
 .appliance .field.name{grid-column:1/-1}
 .settings,.results{grid-template-columns:1fr}
 .bar-row{grid-template-columns:90px 1fr 65px}
}
</style>
</head>
<body>
<header>
 <div><h1>WattWise</h1><p>SMART ENERGY ESTIMATOR</p></div>
 <div>Smart Energy Management</div>
</header>
<main>
 <section class="card">
  <h2>Appliance Details</h2>
  <p class="muted">Enter the appliances you use and their daily usage.</p>
  <div id="appliances"></div>
  <div class="actions">
   <button type="button" onclick="addAppliance()">+ Add Appliance</button>
   <button type="button" class="secondary" onclick="saveAppliances()">Save Details</button>
   <button type="button" class="secondary" onclick="loadAppliances()">Load Saved Details</button>
  </div>
 </section>

 <section class="card">
  <h2>Billing Settings</h2>
  <div class="settings">
   <div class="field"><small>ELECTRICITY TARIFF (₹/UNIT)</small>
    <input id="tariff" type="number" min="0" step="any" value="8">
   </div>
   <div class="field"><small>BILLING MODE</small>
    <select id="billingMode">
     <option value="fixed">Fixed Rate</option>
     <option value="slab">Slab-Based Billing</option>
    </select>
   </div>
   <div class="field"><small>MONTHLY BUDGET (₹)</small>
    <input id="budget" type="number" min="0" step="any" placeholder="Enter budget">
   </div>
  </div>
  <div class="actions">
   <button onclick="calculate()">Calculate My Cost</button>
  </div>
 </section>

 <section class="card">
  <h2>Energy Consumption and Cost</h2>
  <div class="results">
   <div class="result"><small>Daily Energy</small><strong id="dailyEnergy">0 kWh</strong></div>
   <div class="result"><small>Monthly Energy</small><strong id="monthlyEnergy">0 kWh</strong></div>
   <div class="result"><small>Yearly Energy</small><strong id="yearlyEnergy">0 kWh</strong></div>
   <div class="result"><small>Daily Cost</small><strong id="dailyCost">₹0.00</strong></div>
   <div class="result"><small>Monthly Cost</small><strong id="monthlyCost">₹0.00</strong></div>
   <div class="result"><small>Yearly Cost</small><strong id="yearlyCost">₹0.00</strong></div>
  </div>
  <div id="budgetStatus" class="status">Calculate your energy usage to view your budget status.</div>
 </section>

 <section class="card">
  <h2>Energy Usage Insights</h2>
  <p id="highest">Highest-consuming appliance: --</p>
  <p id="recommendation">Savings recommendation: --</p>
  <h3>Daily Energy by Appliance</h3>
  <div id="chart" class="muted">Calculate to view the chart.</div>
 </section>

 <section class="card">
  <h2>Calculation History</h2>
  <div class="actions"><button class="secondary" onclick="loadHistory()">View History</button></div>
  <div style="overflow-x:auto;margin-top:14px">
   <table>
    <thead><tr><th>Date</th><th>Daily Energy</th><th>Daily Cost</th><th>Monthly Cost</th><th>Tariff</th></tr></thead>
    <tbody id="history"><tr><td colspan="5">Select View History to load records.</td></tr></tbody>
   </table>
  </div>
 </section>

 <section class="card">
  <h2>Export Report</h2>
  <p class="muted">Download your latest calculation as a CSV report.</p>
  <button onclick="exportCSV()">Export CSV Report</button>
 </section>
</main>

<script>
const applianceBox=document.getElementById('appliances');
let lastCalculation=null;

function addAppliance(name='',power='',hours=''){
 const row=document.createElement('div');
 row.className='appliance';
 row.innerHTML=`
  <div class="field name"><small>APPLIANCE *</small>
   <input class="a-name" placeholder="Appliance *">
  </div>
  <div class="field"><small>POWER (W) *</small>
   <input class="a-power" type="number" min="0" step="any" placeholder="Watts *">
  </div>
  <div class="field"><small>HOURS/DAY *</small>
   <input class="a-hours" type="number" min="0" max="24" step="any" placeholder="Hours/day *">
  </div>
  <button type="button" class="danger" onclick="this.parentElement.remove()">Remove</button>`;
 row.querySelector('.a-name').value=name;
 row.querySelector('.a-power').value=power;
 row.querySelector('.a-hours').value=hours;
 applianceBox.appendChild(row);
}

function getAppliances(){
 const rows=[...document.querySelectorAll('.appliance')];
 if(!rows.length)throw new Error('Add at least one appliance.');
 return rows.map((row,index)=>{
  const name=row.querySelector('.a-name').value.trim();
  const power=Number(row.querySelector('.a-power').value);
  const hours=Number(row.querySelector('.a-hours').value);
  if(!name||!Number.isFinite(power)||power<=0||!Number.isFinite(hours)||hours<=0||hours>24){
   throw new Error('Please enter a valid name, wattage and daily usage for appliance '+(index+1)+'.');
  }
  return {name,power,hours};
 });
}

async function api(url,options={}){
 const response=await fetch(url,options);
 const data=await response.json();
 if(!response.ok)throw new Error(data.error||'Request failed.');
 return data;
}

async function saveAppliances(){
 try{
  const items=getAppliances();
  const body=new URLSearchParams();
  body.set('count',items.length);
  items.forEach((a,i)=>{
   body.set('name'+i,a.name);
   body.set('power'+i,a.power);
   body.set('hours'+i,a.hours);
  });
  await api('/api/appliances',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});
  localStorage.setItem('wattwise-appliances',JSON.stringify(items));
  alert('Appliance details saved successfully.');
 }catch(e){alert(e.message);}
}

async function loadAppliances(){
 try{
  const items=await api('/api/appliances');
  if(items.length){
   applianceBox.innerHTML='';
   items.forEach(a=>addAppliance(a.name,a.power,a.hours));
   alert('Saved appliance details loaded from MySQL.');
   return;
  }
  const local=JSON.parse(localStorage.getItem('wattwise-appliances')||'[]');
  if(local.length){
   applianceBox.innerHTML='';
   local.forEach(a=>addAppliance(a.name,a.power,a.hours));
   alert('Loaded locally saved appliance details.');
  }else alert('No saved appliance details found.');
 }catch(e){alert('Could not load saved details: '+e.message);}
}

function calculate(){
 try{
  const items=getAppliances();
  const tariff=Number(document.getElementById('tariff').value);
  const budgetValue=document.getElementById('budget').value;
  const budget=budgetValue===''?null:Number(budgetValue);
  const mode=document.getElementById('billingMode').value;
  if(!Number.isFinite(tariff)||tariff<0)throw new Error('Enter a valid tariff.');
  if(budget!==null&&(!Number.isFinite(budget)||budget<0))throw new Error('Enter a valid budget.');

  const dailyEnergy=items.reduce((sum,a)=>sum+(a.power*a.hours/1000),0);
  const monthlyEnergy=dailyEnergy*30;
  const yearlyEnergy=dailyEnergy*365;
  const bill=(units)=>{
   if(mode==='fixed')return units*tariff;
   let remaining=units,total=0;
   const slabs=[{limit:100,rate:3},{limit:100,rate:5},{limit:100,rate:7},{limit:Infinity,rate:8}];
   for(const slab of slabs){
    const used=Math.min(remaining,slab.limit);
    total+=used*slab.rate;
    remaining-=used;
    if(remaining<=0)break;
   }
   return total;
  };
  const dailyCost=bill(monthlyEnergy)/30;
  const monthlyCost=bill(monthlyEnergy);
  const yearlyCost=bill(yearlyEnergy);
  const fmt=n=>'₹'+n.toFixed(2);
  document.getElementById('dailyEnergy').textContent=dailyEnergy.toFixed(3)+' kWh';
  document.getElementById('monthlyEnergy').textContent=monthlyEnergy.toFixed(3)+' kWh';
  document.getElementById('yearlyEnergy').textContent=yearlyEnergy.toFixed(3)+' kWh';
  document.getElementById('dailyCost').textContent=fmt(dailyCost);
  document.getElementById('monthlyCost').textContent=fmt(monthlyCost);
  document.getElementById('yearlyCost').textContent=fmt(yearlyCost);

  const status=document.getElementById('budgetStatus');
  if(budget===null)status.textContent='Enter a monthly budget to check your budget status.';
  else if(monthlyCost<=budget)status.textContent='Within budget. Estimated remaining amount: '+fmt(budget-monthlyCost);
  else status.textContent='Over budget by '+fmt(monthlyCost-budget)+'. Consider reducing appliance usage.';

  const usage=items.map(a=>({...a,energy:a.power*a.hours/1000}));
  usage.sort((a,b)=>b.energy-a.energy);
  const highest=usage[0];
  document.getElementById('highest').textContent='Highest-consuming appliance: '+highest.name+' ('+highest.energy.toFixed(3)+' kWh/day)';
  document.getElementById('recommendation').textContent='Savings recommendation: Reduce '+highest.name+' usage by 1 hour per day to save approximately '+fmt(highest.power/1000*30*(mode==='fixed'?tariff:5))+' per month (estimated).';

  const max=Math.max(...usage.map(a=>a.energy),0.001);
  document.getElementById('chart').innerHTML=usage.map(a=>
   '<div class="bar-row"><span>'+escapeHtml(a.name)+'</span><div class="bar"><span style="width:'+Math.max(2,a.energy/max*100)+'%"></span></div><b>'+a.energy.toFixed(3)+' kWh</b></div>'
  ).join('');

  lastCalculation={items,tariff,mode,dailyEnergy,dailyCost,monthlyEnergy,monthlyCost,yearlyEnergy,yearlyCost};
  saveHistory(lastCalculation);
 }catch(e){alert(e.message);}
}

function escapeHtml(value){
 return String(value).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

async function saveHistory(c){
 try{
  const body=new URLSearchParams({
   dailyEnergy:c.dailyEnergy,dailyCost:c.dailyCost,
   monthlyEnergy:c.monthlyEnergy,monthlyCost:c.monthlyCost,tariff:c.tariff
  });
  await api('/api/history/save',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});
 }catch(e){console.error('History save failed:',e.message);}
}

async function loadHistory(){
 const tbody=document.getElementById('history');
 try{
  const rows=await api('/api/history');
  if(!rows.length){tbody.innerHTML='<tr><td colspan="5">No calculation history available.</td></tr>';return;}
  tbody.innerHTML=rows.map(r=>'<tr><td>'+escapeHtml(r.date)+'</td><td>'+Number(r.dailyEnergy).toFixed(3)+' kWh</td><td>₹'+Number(r.dailyCost).toFixed(2)+'</td><td>₹'+Number(r.monthlyCost).toFixed(2)+'</td><td>₹'+Number(r.tariff).toFixed(2)+'</td></tr>').join('');
 }catch(e){tbody.innerHTML='<tr><td colspan="5">'+escapeHtml(e.message)+'</td></tr>';}
}

function exportCSV(){
 if(!lastCalculation){alert('Calculate your energy cost first.');return;}
 const c=lastCalculation;
 const rows=[
  ['WattWise - Smart Energy Estimator'],
  ['Billing mode',c.mode==='fixed'?'Fixed Rate':'Slab-Based Billing'],
  ['Tariff',c.tariff],
  ['Daily Energy (kWh)',c.dailyEnergy],
  ['Daily Cost (Rs)',c.dailyCost],
  ['Monthly Energy (kWh)',c.monthlyEnergy],
  ['Monthly Cost (Rs)',c.monthlyCost],
  ['Yearly Energy (kWh)',c.yearlyEnergy],
  ['Yearly Cost (Rs)',c.yearlyCost],
  [],
  ['Appliance','Power (W)','Hours/day','Daily Energy (kWh)'],
  ...c.items.map(a=>[a.name,a.power,a.hours,a.power*a.hours/1000])
 ];
 const csv=rows.map(r=>r.map(v=>'"'+String(v??'').replace(/"/g,'""')+'"').join(',')).join('\\r\\n');
 const blob=new Blob([csv],{type:'text/csv;charset=utf-8;'});
 const url=URL.createObjectURL(blob);
 const link=document.createElement('a');
 link.href=url;link.download='WattWise_Report.csv';link.click();
 URL.revokeObjectURL(url);
}

addAppliance();
</script>
</body>
</html>
""";

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080"));
        DatabaseConnection.initSchema();
        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", BrowserServer::handleRequest);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("WattWise browser app is running.");
        System.out.println("Listening on port " + port);
    }

    private static void handleRequest(HttpExchange exchange)
            throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/".equals(path) && "GET".equals(method)) {
                send(exchange, 200, PAGE, "text/html; charset=UTF-8");
            } else if ("/api/appliances".equals(path)
                    && "GET".equals(method)) {
                send(exchange, 200, getAppliances(), "application/json");
            } else if ("/api/appliances".equals(path)
                    && "POST".equals(method)) {
                saveAppliances(readForm(exchange));
                send(exchange, 200, "{\"success\":true}", "application/json");
            } else if ("/api/history/save".equals(path)
                    && "POST".equals(method)) {
                saveHistory(readForm(exchange));
                send(exchange, 200, "{\"success\":true}", "application/json");
            } else if ("/api/history".equals(path)
                    && "GET".equals(method)) {
                send(exchange, 200, getHistory(), "application/json");
            } else {
                send(exchange, 404, "{\"error\":\"Not found\"}",
                        "application/json");
            }
        } catch (Exception exception) {
            String message = exception.getMessage() == null
                    ? "Server error" : exception.getMessage();
            send(exchange, 500,
                    "{\"error\":\"" + jsonEscape(message) + "\"}",
                    "application/json");
        } finally {
            exchange.close();
        }
    }

    private static String getAppliances() throws SQLException {
        StringBuilder json = new StringBuilder("[");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT name, power_watts, hours_per_day "
                             + "FROM appliances ORDER BY id");
             ResultSet result = statement.executeQuery()) {

            boolean first = true;
            while (result.next()) {
                if (!first) json.append(",");
                first = false;
                json.append("{")
                        .append("\"name\":\"")
                        .append(jsonEscape(result.getString("name")))
                        .append("\",\"power\":")
                        .append(result.getDouble("power_watts"))
                        .append(",\"hours\":")
                        .append(result.getDouble("hours_per_day"))
                        .append("}");
            }
        }
        return json.append("]").toString();
    }

    private static void saveAppliances(Map<String, String> form)
            throws SQLException {
        int count = Integer.parseInt(form.getOrDefault("count", "0"));
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException(
                    "Add between 1 and 100 appliances.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM appliances")) {
                    delete.executeUpdate();
                }

                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO appliances "
                                + "(name, power_watts, hours_per_day) "
                                + "VALUES (?, ?, ?)")) {
                    for (int i = 0; i < count; i++) {
                        String name = form.get("name" + i);
                        double power = Double.parseDouble(
                                form.get("power" + i));
                        double hours = Double.parseDouble(
                                form.get("hours" + i));

                        if (name == null || name.isBlank()
                                || !Double.isFinite(power) || power <= 0
                                || !Double.isFinite(hours)
                                || hours <= 0 || hours > 24) {
                            throw new IllegalArgumentException(
                                    "Invalid appliance details.");
                        }

                        insert.setString(1, name.trim());
                        insert.setDouble(2, power);
                        insert.setDouble(3, hours);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void saveHistory(Map<String, String> form)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO calculation_history "
                             + "(daily_energy_kwh, daily_cost, "
                             + "monthly_energy_kwh, monthly_cost, "
                             + "tariff_per_kwh) VALUES (?, ?, ?, ?, ?)")) {
            statement.setDouble(1, number(form, "dailyEnergy"));
            statement.setDouble(2, number(form, "dailyCost"));
            statement.setDouble(3, number(form, "monthlyEnergy"));
            statement.setDouble(4, number(form, "monthlyCost"));
            statement.setDouble(5, number(form, "tariff"));
            statement.executeUpdate();
        }
    }

    private static String getHistory() throws SQLException {
        StringBuilder json = new StringBuilder("[");
        try (Connection connection = DatabaseConnection.getConnection()) {
            String dateColumn = findDateColumn(connection);
            String sql = "SELECT id, " + dateColumn
                    + " AS history_date, daily_energy_kwh, daily_cost, "
                    + "monthly_energy_kwh, monthly_cost, tariff_per_kwh "
                    + "FROM calculation_history ORDER BY id DESC";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {
                boolean first = true;
                while (result.next()) {
                    if (!first) json.append(",");
                    first = false;

                    Object date = result.getObject("history_date");
                    json.append("{")
                            .append("\"date\":\"")
                            .append(jsonEscape(String.valueOf(date)))
                            .append("\",\"dailyEnergy\":")
                            .append(result.getDouble("daily_energy_kwh"))
                            .append(",\"dailyCost\":")
                            .append(result.getDouble("daily_cost"))
                            .append(",\"monthlyEnergy\":")
                            .append(result.getDouble("monthly_energy_kwh"))
                            .append(",\"monthlyCost\":")
                            .append(result.getDouble("monthly_cost"))
                            .append(",\"tariff\":")
                            .append(result.getDouble("tariff_per_kwh"))
                            .append("}");
                }
            }
        }
        return json.append("]").toString();
    }

    private static String findDateColumn(Connection connection)
            throws SQLException {
        Set<String> columns = new HashSet<>();
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet result = metadata.getColumns(
                connection.getCatalog(), null, "calculation_history", null)) {
            while (result.next()) {
                columns.add(result.getString("COLUMN_NAME").toLowerCase());
            }
        }

        for (String candidate : List.of(
                "calculated_at", "timestamp", "created_at")) {
            if (columns.contains(candidate)) {
                return "`" + candidate + "`";
            }
        }
        return "id";
    }

    private static double number(Map<String, String> form, String key) {
        double value = Double.parseDouble(form.get(key));
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Invalid number: " + key);
        }
        return value;
    }

    private static Map<String, String> readForm(HttpExchange exchange)
            throws IOException {
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
        Map<String, String> values = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            values.put(key, value);
        }
        return values;
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void send(HttpExchange exchange, int status,
                             String response, String contentType)
            throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
