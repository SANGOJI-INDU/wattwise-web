
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
html,body{margin:0;padding:0}
body{font-family:"Segoe UI",system-ui,-apple-system,Arial,sans-serif;background:#F3F6FB;color:#142B4A}
header{background:#142B4A;padding:22px 30px}
.brand{display:flex;align-items:center;gap:13px}
.logo{width:44px;height:44px;border-radius:12px;background:#2563EB;color:#fff;font-size:22px;font-weight:bold;display:flex;align-items:center;justify-content:center}
.brand h1{margin:0;font-size:25px;font-weight:bold;color:#fff;line-height:1.2}
.brand p{margin:3px 0 0;font-size:10px;color:#CBD5E1}
main{padding:26px 30px 30px;display:flex;flex-direction:column;gap:20px}
.welcome{font-size:23px;font-weight:bold;color:#142B4A;margin:0}
.desc{font-size:13px;color:#64748B;margin:7px 0 0}
.card{background:#fff;border:1px solid #E2E8F0;border-radius:14px;padding:20px}
.title{font-size:16px;font-weight:bold;color:#142B4A;margin:0}
.col{display:flex;flex-direction:column}
.g18{gap:18px}.g14{gap:14px}.g12{gap:12px}.g10{gap:10px}.g6{gap:6px}
.small{font-size:11px;font-weight:bold;color:#142B4A}
.hint{font-size:11px;color:#64748B}
input[type=text]{height:40px;width:100%;background:#fff;border:1px solid #D8E0EC;border-radius:8px;padding:0 12px;font-size:13px;font-family:inherit;color:#142B4A;outline:none}
input[type=text]:focus{border-color:#2563EB}
input[type=text]:disabled{background:#F1F5F9;color:#94A3B8}
select{height:32px;width:100%;background:#ececec;border:1px solid #c4c4c4;border-radius:4px;padding:0 8px;font-size:13px;font-family:inherit;color:#142B4A}
.arow{display:flex;gap:8px;align-items:center}
.arow .n{flex:1;min-width:0}.arow .p{width:100px;flex:none}.arow .h{width:105px;flex:none}
.rm{flex:none;width:40px;height:40px;border:0;border-radius:8px;background:#FEE2E2;color:#B91C1C;font-size:16px;font-weight:bold;cursor:pointer}
button{font-family:inherit}
.btn{border:0;border-radius:8px;padding:10px;font-size:13px;font-weight:bold;cursor:pointer;width:100%}
.btn.blue{background:#2563EB;color:#fff}
.btn.light{background:#E8F0FF;color:#2563EB}
.btn:hover{filter:brightness(.96)}
.btn.big{height:46px;border-radius:10px;font-size:14px}
.btn.mid{height:42px}
.trio{display:flex;gap:10px}.trio .btn{flex:1}
.pair{display:flex;gap:14px}.pair .card{flex:1;min-width:0}
.cap{font-size:12px;color:#64748B}
.val{font-size:21px;font-weight:bold;word-break:break-word}
.blue-t{color:#2563EB}.green-t{color:#15803D}.red-t{color:#B91C1C}
#budgetStatus{font-size:14px;font-weight:bold;color:#64748B}
#highest{font-size:14px;font-weight:bold;color:#2563EB}
.insight{font-size:13px;color:#142B4A;padding:7px 0}
#saveRec{font-size:13px;color:#142B4A}
#message{font-size:12px;color:#64748B}
#chart{width:100%;height:340px;min-height:280px}
.ov{position:fixed;inset:0;background:rgba(15,23,42,.45);display:none;align-items:center;justify-content:center;z-index:50;padding:16px}
.ov.show{display:flex}
.dlg{background:#fff;border-radius:6px;min-width:300px;max-width:520px;width:100%;box-shadow:0 10px 40px rgba(0,0,0,.35)}
.dlg .dt{padding:10px 14px;font-size:13px;color:#334155;border-bottom:1px solid #e5e7eb}
.dlg .dh{padding:16px 18px 6px;font-size:16px;font-weight:bold;color:#142B4A}
.dlg .db{padding:6px 18px 18px;font-size:13px;color:#334155;word-break:break-word}
.dlg .df{padding:10px 14px;background:#f3f4f6;display:flex;justify-content:flex-end;border-radius:0 0 6px 6px}
.dlg .df button{border:1px solid #bbb;background:#e5e7eb;border-radius:3px;padding:6px 26px;cursor:pointer}
.hist{max-width:1050px;width:100%;background:#fff;border-radius:6px;box-shadow:0 10px 40px rgba(0,0,0,.35)}
.hist .dt{display:flex;justify-content:space-between;align-items:center;padding:10px 14px;font-size:13px;color:#334155;border-bottom:1px solid #e5e7eb}
.hist .dt button{border:0;background:none;font-size:18px;cursor:pointer;color:#475569}
.hist .tb{padding:12px}
.hist .tb button{border:1px solid #bbb;background:#e5e7eb;border-radius:3px;padding:6px 14px;cursor:pointer;margin-bottom:10px}
.hist .sc{overflow:auto;max-height:55vh;border:1px solid #d1d5db}
table{width:100%;border-collapse:collapse;font-size:13px}
th{background:#f3f4f6;text-align:left;padding:8px 10px;border-bottom:1px solid #d1d5db;border-right:1px solid #e5e7eb;position:sticky;top:0;white-space:nowrap}
td{padding:7px 10px;border-bottom:1px solid #eef0f3;white-space:nowrap}
@media(max-width:640px){
 main{padding:20px 14px}header{padding:18px 14px}
 .pair{flex-direction:column}.trio{flex-direction:column}
 .arow .p{width:76px}.arow .h{width:80px}
}
</style>
</head>
<body>
<header>
 <div class="brand">
  <div class="logo">W</div>
  <div><h1>WattWise</h1><p>SMART ENERGY ESTIMATOR</p></div>
 </div>
</header>
<main>
 <div>
  <h2 class="welcome">Electricity Cost Calculator</h2>
  <p class="desc">Estimate electricity use, understand costs, and find ways to save.</p>
 </div>

 <section class="card col g18">
  <h3 class="title">Your appliances</h3>
  <div id="rows" class="col g10"></div>
  <button class="btn light" type="button" onclick="addRow()">+ Add Appliance</button>

  <div class="col g6">
   <div class="small">ELECTRICITY RATE</div>
   <div class="hint">Rate in ₹ per kWh (Fixed Rate mode only)</div>
   <input id="tariff" type="text" inputmode="decimal" placeholder="Example: 8">
  </div>

  <div class="col g6">
   <div class="small">BILLING MODE</div>
   <div class="hint">Choose how your electricity bill is estimated</div>
   <select id="mode" onchange="modeChanged()">
    <option>Fixed Rate</option>
    <option>Slab-Based Billing</option>
   </select>
  </div>

  <div class="col g6">
   <div class="small">MONTHLY ELECTRICITY BUDGET</div>
   <div class="hint">Your maximum target bill in rupees</div>
   <input id="budget" type="text" inputmode="decimal" placeholder="Example: 1000">
  </div>

  <div class="trio">
   <button class="btn blue" type="button" onclick="saveDetails()">Save Details</button>
   <button class="btn light" type="button" onclick="loadSaved(true)">Load Saved Details</button>
   <button class="btn light" type="button" onclick="loadFromDb()">Load from MySQL</button>
  </div>
 </section>

 <button class="btn blue big" type="button" onclick="calculate()">Calculate my cost &nbsp;→</button>
 <button class="btn light mid" type="button" onclick="showHistory()">View History</button>

 <h3 class="title">Daily estimate</h3>
 <div class="pair">
  <div class="card col g10"><div class="cap">Daily energy consumption</div><div class="val blue-t" id="dE">-- kWh</div></div>
  <div class="card col g10"><div class="cap">Daily estimated cost</div><div class="val green-t" id="dC">₹ --</div></div>
 </div>

 <h3 class="title">Monthly estimate</h3>
 <div class="pair">
  <div class="card col g10"><div class="cap">Monthly energy (30 days)</div><div class="val blue-t" id="mE">-- kWh</div></div>
  <div class="card col g10"><div class="cap">Monthly estimated cost</div><div class="val green-t" id="mC">₹ --</div></div>
 </div>

 <h3 class="title">Yearly estimate</h3>
 <div class="pair">
  <div class="card col g10"><div class="cap">Yearly energy (365 days)</div><div class="val blue-t" id="yE">-- kWh</div></div>
  <div class="card col g10"><div class="cap">Yearly estimated cost</div><div class="val green-t" id="yC">₹ --</div></div>
 </div>

 <section class="card col g10">
  <h3 class="title">Monthly budget check</h3>
  <div id="budgetStatus">Enter a monthly budget to check your estimate.</div>
 </section>

 <section class="card col g12">
  <h3 class="title">Consumption insights</h3>
  <div id="highest">Calculate to discover your highest-consuming appliance.</div>
  <div class="hint">Appliance-wise share of daily consumption</div>
  <div id="insights" class="col g10"></div>
 </section>

 <section class="card col g14">
  <h3 class="title">Energy-saving recommendations</h3>
  <div id="saveRec">Calculate your usage to receive an energy-saving suggestion.</div>
  <div class="pair">
   <div class="card col g10"><div class="cap">Potential daily savings</div><div class="val green-t" id="sD">₹ --</div></div>
   <div class="card col g10"><div class="cap">Potential monthly savings</div><div class="val green-t" id="sM">₹ --</div></div>
  </div>
  <div class="pair">
   <div class="card col g10"><div class="cap">Potential yearly savings</div><div class="val green-t" id="sY">₹ --</div></div>
   <div class="card col g10"><div class="cap">Revised monthly bill</div><div class="val blue-t" id="sR">₹ --</div></div>
  </div>
  <div class="hint">Illustrative estimates assume the same tariff and usage pattern.</div>
 </section>

 <section class="card col g12">
  <h3 class="title">Energy usage comparison</h3>
  <div class="hint">Compare the daily energy used by each appliance.</div>
  <div id="chart"></div>
 </section>

 <button class="btn blue" type="button" onclick="exportCsv()">Export CSV Report</button>
 <div id="message">Enter appliance details, tariff, and monthly budget.</div>
</main>

<div class="ov" id="dlgOv">
 <div class="dlg">
  <div class="dt">WattWise</div>
  <div class="dh" id="dlgH"></div>
  <div class="db" id="dlgB"></div>
  <div class="df"><button type="button" onclick="closeDlg()">OK</button></div>
 </div>
</div>

<div class="ov" id="histOv">
 <div class="hist">
  <div class="dt"><span>WattWise | Calculation History</span><button type="button" onclick="closeHist()">×</button></div>
  <div class="tb">
   <button type="button" onclick="loadHistory()">Refresh History</button>
   <div class="sc">
    <table>
     <thead><tr><th>ID</th><th>Calculated At</th><th>Daily kWh</th><th>Daily Cost (₹)</th><th>Monthly kWh</th><th>Monthly Cost (₹)</th><th>Tariff (₹/kWh)</th></tr></thead>
     <tbody id="histBody"></tbody>
    </table>
   </div>
  </div>
 </div>
</div>

<script>
var SLAB='Slab-Based Billing';
var last=null;
var rowsBox=document.getElementById('rows');
var $=function(id){return document.getElementById(id);};

function esc(v){return String(v).replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];});}

function addRow(name,power,hours){
 var d=document.createElement('div');
 d.className='arow';
 d.innerHTML='<input type="text" class="n" placeholder="Appliance *">'
  +'<input type="text" class="p" inputmode="decimal" placeholder="Watts *">'
  +'<input type="text" class="h" inputmode="decimal" placeholder="Hours/day *">'
  +'<button type="button" class="rm">×</button>';
 d.querySelector('.n').value=name||'';
 d.querySelector('.p').value=power||'';
 d.querySelector('.h').value=hours||'';
 d.querySelector('.rm').onclick=function(){
  if(rowsBox.children.length>1){d.remove();}
  else{showError('At least one appliance is required.');}
 };
 rowsBox.appendChild(d);
}

function modeChanged(){
 var slab=$('mode').value===SLAB;
 $('tariff').disabled=slab;
 $('tariff').placeholder=slab?'Not required for slab billing':'Example: 8';
}

function setMsg(text,color){var m=$('message');m.textContent=text;m.style.color=color;}
function setStatus(t){setMsg(t,'#15803D');}
function openDlg(h,b){$('dlgH').textContent=h;$('dlgB').textContent=b;$('dlgOv').classList.add('show');}
function closeDlg(){$('dlgOv').classList.remove('show');}
function showSuccess(t){openDlg('Success',t);}
function showError(t){setMsg(t,'#B91C1C');openDlg('Please check your details',t);}

function num(s){return Number(String(s).trim());}

function slabBill(units){
 var rem=units,bill=0,lim=[100,200,300],rate=[1.5,3.5,5.5],prev=0;
 for(var i=0;i<lim.length;i++){
  var inSlab=Math.min(rem,lim[i]-prev);
  if(inSlab>0){bill+=inSlab*rate[i];rem-=inSlab;}
  prev=lim[i];
  if(rem<=0)return bill;
 }
 if(rem>0)bill+=rem*7;
 return bill;
}
function calcBill(energy,tariff){return $('mode').value===SLAB?slabBill(energy):energy*tariff;}

function readRows(){
 var out=[];
 var list=rowsBox.children;
 for(var i=0;i<list.length;i++){
  var name=list[i].querySelector('.n').value.trim();
  var pTxt=list[i].querySelector('.p').value.trim();
  var hTxt=list[i].querySelector('.h').value.trim();
  if(!name){showError('Appliance name is mandatory.');return null;}
  if(!pTxt){showError('Power in Watts is mandatory.');return null;}
  if(!hTxt){showError('Hours per day is mandatory.');return null;}
  var power=num(pTxt),hours=num(hTxt);
  if(isNaN(power)||isNaN(hours)){showError('Please enter valid numeric values for power, hours, tariff, and budget.');return null;}
  if(!isFinite(power)||power<=0){showError('Appliance power must be greater than 0 Watts.');return null;}
  if(!isFinite(hours)||hours<=0||hours>24){showError('Appliance hours must be greater than 0 and at most 24.');return null;}
  out.push({name:name,power:power,hours:hours});
 }
 return out;
}

function readSettings(){
 var slab=$('mode').value===SLAB;
 var tariff=0;
 if(!slab){
  var t=$('tariff').value.trim();
  if(!t){showError('Please enter the electricity rate.');return null;}
  tariff=num(t);
  if(isNaN(tariff)){showError('Please enter valid numeric values for power, hours, tariff, and budget.');return null;}
  if(!isFinite(tariff)||tariff<0){showError('Tariff must be zero or greater.');return null;}
 }
 var b=$('budget').value.trim();
 if(!b){showError('Please enter your monthly electricity budget.');return null;}
 var budget=num(b);
 if(isNaN(budget)){showError('Please enter valid numeric values for power, hours, tariff, and budget.');return null;}
 if(!isFinite(budget)||budget<0){showError('Monthly budget must be zero or greater.');return null;}
 return {slab:slab,tariff:tariff,budget:budget};
}

async function api(url,options){
 var r=await fetch(url,options||{});
 var data=await r.json();
 if(!r.ok)throw new Error(data.error||'Request failed.');
 return data;
}
function formBody(obj){var b=new URLSearchParams();for(var k in obj)b.set(k,obj[k]);return b;}

function calculate(){
 last=null;
 var s=readSettings();if(!s)return false;
 var items=readRows();if(!items)return false;

 var total=0,hi=-1,hiName='',hiPower=0,hiHours=0;
 var names=[],energies=[];
 items.forEach(function(a){
  var used=a.power*a.hours/1000;
  total+=used;names.push(a.name);energies.push(used);
  if(used>hi){hi=used;hiName=a.name;hiPower=a.power;hiHours=a.hours;}
 });

 var monthlyEnergy=total*30,yearlyEnergy=total*365;
 var dailyCost,monthlyCost,yearlyCost;
 if(s.slab){
  monthlyCost=calcBill(monthlyEnergy,0);
  dailyCost=monthlyCost/30;
  yearlyCost=calcBill(yearlyEnergy/12,0)*12;
 }else{
  dailyCost=total*s.tariff;
  monthlyCost=dailyCost*30;
  yearlyCost=dailyCost*365;
 }
 if(![total,dailyCost,monthlyCost,yearlyCost,yearlyEnergy].every(isFinite)){
  showError('The calculated values are too large to display.');return false;
 }
 var eff=monthlyEnergy>0?monthlyCost/monthlyEnergy:s.tariff;

 $('dE').textContent=total.toFixed(3)+' kWh/day';
 $('dC').textContent='₹'+dailyCost.toFixed(2)+'/day';
 $('mE').textContent=monthlyEnergy.toFixed(3)+' kWh';
 $('mC').textContent='₹'+monthlyCost.toFixed(2);
 $('yE').textContent=yearlyEnergy.toFixed(3)+' kWh';
 $('yC').textContent='₹'+yearlyCost.toFixed(2);

 $('highest').textContent='Highest-consuming appliance: '+hiName+' ('+hi.toFixed(3)+' kWh/day)';
 $('insights').innerHTML=names.map(function(n,i){
  var pct=total>0?energies[i]/total*100:0;
  return '<div class="insight">'+esc(n)+' — '+energies[i].toFixed(3)+' kWh/day ('+pct.toFixed(1)+'%)</div>';
 }).join('');

 var hoursReduced=Math.min(1.0,hiHours);
 var dailySavedKwh=hiPower*hoursReduced/1000;
 var monthlySavedKwh=dailySavedKwh*30,yearlySavedKwh=dailySavedKwh*365;
 var sD,sM,sY,rev;
 if(s.slab){
  var redM=calcBill(Math.max(0,monthlyEnergy-monthlySavedKwh),0);
  var redY=calcBill(Math.max(0,yearlyEnergy-yearlySavedKwh)/12,0)*12;
  sM=Math.max(0,monthlyCost-redM);
  sY=Math.max(0,yearlyCost-redY);
  sD=sM/30;rev=redM;
 }else{
  sD=dailySavedKwh*s.tariff;sM=sD*30;sY=sD*365;
  rev=Math.max(0,monthlyCost-sM);
 }
 $('saveRec').textContent='Recommendation: Reduce '+hiName+' usage by '+hoursReduced.toFixed(1)+' hour(s) per day. This could save '+dailySavedKwh.toFixed(3)+' kWh daily.';
 $('sD').textContent='₹'+sD.toFixed(2)+'/day';
 $('sM').textContent='₹'+sM.toFixed(2)+'/month';
 $('sY').textContent='₹'+sY.toFixed(2)+'/year';
 $('sR').textContent='₹'+rev.toFixed(2);

 var bs=$('budgetStatus');
 if(monthlyCost>s.budget){
  bs.textContent='⚠ Over budget by ₹'+(monthlyCost-s.budget).toFixed(2)+'. Estimated bill: ₹'+monthlyCost.toFixed(2)+' | Budget: ₹'+s.budget.toFixed(2);
  bs.style.color='#B91C1C';
 }else{
  bs.textContent='✓ Within budget! ₹'+(s.budget-monthlyCost).toFixed(2)+' remaining. Estimated bill: ₹'+monthlyCost.toFixed(2)+' | Budget: ₹'+s.budget.toFixed(2);
  bs.style.color='#15803D';
 }

 var counts={},chartNames=[];
 names.forEach(function(n){counts[n]=(counts[n]||0)+1;chartNames.push(counts[n]===1?n:n+' ('+counts[n]+')');});
 drawChart(chartNames,energies);

 last={items:items,names:names,energies:energies,mode:$('mode').value,eff:eff,total:total,dailyCost:dailyCost,
  monthlyEnergy:monthlyEnergy,monthlyCost:monthlyCost,yearlyEnergy:yearlyEnergy,yearlyCost:yearlyCost,
  sD:sD,sM:sM,sY:sY,rev:rev};

 api('/api/history/save',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
  body:formBody({dailyEnergy:total,dailyCost:dailyCost,monthlyEnergy:monthlyEnergy,monthlyCost:monthlyCost,tariff:eff})})
 .then(function(){setStatus('Calculation updated and saved to MySQL successfully.');})
 .catch(function(e){setStatus('Calculation completed, but saving to MySQL failed: '+e.message);});
 return true;
}

/* ---------- chart ---------- */
var chartData=null;
function niceStep(max){
 var raw=max/6,pow=Math.pow(10,Math.floor(Math.log10(raw))),f=raw/pow;
 var m=f<=1?1:f<=2?2:f<=5?5:10;
 return m*pow;
}
function drawChart(names,values){
 chartData={names:names,values:values};
 var box=$('chart');
 var W=Math.max(box.clientWidth,300),H=340;
 var L=70,R=24,T=52,B=76;
 var pw=W-L-R,ph=H-T-B;
 var max=Math.max.apply(null,values.concat([0.001]));
 var step=niceStep(max);
 var top=Math.ceil(max/step)*step;
 if(top<=max+step*0.0001)top+=step;
 var svg='<svg xmlns="http://www.w3.org/2000/svg" width="'+W+'" height="'+H+'" font-family="Segoe UI,Arial,sans-serif">';
 svg+='<text x="'+(W/2)+'" y="26" text-anchor="middle" font-size="18" fill="#222">Daily Energy by Appliance</text>';
 var decimals=step<1?Math.min(3,Math.max(1,Math.ceil(-Math.log10(step)))):0;
 for(var v=0;v<=top+step*0.001;v+=step){
  var y=T+ph-(v/top)*ph;
  svg+='<line x1="'+L+'" x2="'+(L+pw)+'" y1="'+y+'" y2="'+y+'" stroke="#d6d6d6" stroke-dasharray="4 3"/>';
  svg+='<text x="'+(L-8)+'" y="'+(y+4)+'" text-anchor="end" font-size="11" fill="#444">'+v.toFixed(decimals)+'</text>';
 }
 svg+='<line x1="'+L+'" x2="'+L+'" y1="'+T+'" y2="'+(T+ph)+'" stroke="#999"/>';
 svg+='<line x1="'+L+'" x2="'+(L+pw)+'" y1="'+(T+ph)+'" y2="'+(T+ph)+'" stroke="#999"/>';
 var n=values.length,slot=pw/n,gap=25;
 var bw=Math.max(8,slot-gap*2/2);
 bw=Math.max(8,slot-gap);
 values.forEach(function(val,i){
  var h=(val/top)*ph,x=L+i*slot+(slot-bw)/2,y=T+ph-h;
  svg+='<rect x="'+x+'" y="'+y+'" width="'+bw+'" height="'+h+'" fill="#F2622C"><title>'+esc(names[i])+': '+val.toFixed(3)+' kWh/day</title></rect>';
  var label=names[i].length>16?names[i].slice(0,15)+'…':names[i];
  svg+='<text x="'+(x+bw/2)+'" y="'+(T+ph+16)+'" text-anchor="middle" font-size="11" fill="#444">'+esc(label)+'</text>';
 });
 svg+='<text x="'+(L+pw/2)+'" y="'+(H-14)+'" text-anchor="middle" font-size="13" fill="#222">Appliance</text>';
 svg+='<text transform="translate(18 '+(T+ph/2)+') rotate(-90)" text-anchor="middle" font-size="13" fill="#222">Energy (kWh/day)</text>';
 svg+='</svg>';
 box.innerHTML=svg;
}
window.addEventListener('resize',function(){if(chartData)drawChart(chartData.names,chartData.values);});

/* ---------- save / load ---------- */
async function saveDetails(){
 var s=readSettings();if(!s)return;
 var items=readRows();if(!items)return;
 var local={tariff:s.tariff,budget:$('budget').value.trim(),billingMode:$('mode').value,items:items};
 try{localStorage.setItem('wattwise-details',JSON.stringify(local));}
 catch(e){showError('Could not save the local backup: '+e.message);return;}
 try{
  var body={count:items.length};
  items.forEach(function(a,i){body['name'+i]=a.name;body['power'+i]=a.power;body['hours'+i]=a.hours;});
  await api('/api/appliances',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:formBody(body)});
  last=null;
  showSuccess('Appliance details saved locally and to MySQL successfully.');
 }catch(e){
  showError('Details were saved locally, but MySQL saving failed: '+e.message);
 }
}

function loadSaved(show){
 var raw=null;
 try{raw=localStorage.getItem('wattwise-details');}catch(e){}
 if(!raw){if(show)showError('No saved details found yet. Save your details first.');return;}
 try{
  var d=JSON.parse(raw);
  if(!d.items||d.items.length<1||d.items.length>100){showError('Saved appliance data is invalid.');return;}
  rowsBox.innerHTML='';
  d.items.forEach(function(a){addRow(a.name,String(a.power),String(a.hours));});
  $('tariff').value=d.tariff===undefined?'':String(d.tariff);
  $('budget').value=d.budget||'';
  if(d.billingMode===SLAB||d.billingMode==='Fixed Rate')$('mode').value=d.billingMode;
  modeChanged();
  if(show)showSuccess('Saved appliance details loaded successfully.');
 }catch(e){showError('Could not load saved details. The save file may be invalid.');}
}

async function loadFromDb(){
 try{
  var list=await api('/api/appliances');
  if(!list.length){showSuccess('No appliance records were found in MySQL. Save appliance details first.');return;}
  rowsBox.innerHTML='';
  list.forEach(function(a){addRow(a.name,String(a.power),String(a.hours));});
  showSuccess(list.length+' appliance record(s) loaded from MySQL successfully. Your current billing settings have been retained.');
 }catch(e){
  showError('Could not load appliance details from MySQL: '+e.message);
 }
}

/* ---------- history ---------- */
function fmtDate(v){
 var s=String(v).replace('T',' ').split('.')[0];
 var p=s.split(' ');
 if(p.length===2&&p[0].split('-').length===3){var d=p[0].split('-');return d[2]+'-'+d[1]+'-'+d[0]+' '+p[1];}
 return s;
}
async function loadHistory(){
 var tb=$('histBody');
 try{
  var rows=await api('/api/history');
  if(!rows.length){tb.innerHTML='<tr><td colspan="7">No calculation history available.</td></tr>';return;}
  tb.innerHTML=rows.map(function(r){
   return '<tr><td>'+esc(r.id)+'</td><td>'+esc(fmtDate(r.date))+'</td><td>'+r.dailyEnergy+'</td><td>'+r.dailyCost+'</td><td>'+r.monthlyEnergy+'</td><td>'+r.monthlyCost+'</td><td>'+r.tariff+'</td></tr>';
  }).join('');
 }catch(e){tb.innerHTML='<tr><td colspan="7">Could not load calculation history: '+esc(e.message)+'</td></tr>';}
}
function showHistory(){$('histOv').classList.add('show');loadHistory();}
function closeHist(){$('histOv').classList.remove('show');}

/* ---------- export ---------- */
function csvq(v){return '"'+String(v).replace(/"/g,'""')+'"';}
function exportCsv(){
 if(!calculate()||!last)return;
 var c=last,nl=String.fromCharCode(13,10);
 var lines=[
  'WattWise Electricity Report',
  'Billing mode,'+csvq(c.mode),
  'Effective average tariff (rupees per kWh),'+c.eff,
  'Daily total energy (kWh),'+c.total,
  'Daily estimated cost (rupees),'+c.dailyCost,
  'Monthly energy (kWh),'+c.monthlyEnergy,
  'Monthly estimated cost (rupees),'+c.monthlyCost,
  'Yearly energy (kWh),'+c.yearlyEnergy,
  'Yearly estimated cost (rupees),'+c.yearlyCost,
  'Potential daily savings (rupees),'+c.sD,
  'Potential monthly savings (rupees),'+c.sM,
  'Potential yearly savings (rupees),'+c.sY,
  'Revised monthly bill estimate (rupees),'+c.rev,
  '',
  'Appliance,Power (W),Hours per day,Daily energy (kWh),Daily cost (rupees),Share of total (%)'
 ];
 c.items.forEach(function(a,i){
  var used=c.energies[i],share=c.total>0?used/c.total*100:0;
  lines.push(csvq(a.name)+','+a.power+','+a.hours+','+used+','+(used*c.eff)+','+share);
 });
 var blob=new Blob([String.fromCharCode(0xFEFF)+lines.join(nl)],{type:'text/csv;charset=utf-8;'});
 var url=URL.createObjectURL(blob);
 var a=document.createElement('a');
 a.href=url;a.download='WattWise_Report.csv';
 document.body.appendChild(a);a.click();a.remove();
 URL.revokeObjectURL(url);
 showSuccess('CSV report exported successfully as WattWise_Report.csv');
}

addRow();
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
                            .append("\"id\":")
                            .append(result.getLong("id"))
                            .append(",\"date\":\"")
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
