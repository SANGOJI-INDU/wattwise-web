# WattWise – Render deployment

1. Create a free MySQL database (Aiven / TiDB Cloud / Railway). Note host, port, user, password, db name.
2. Push this folder to a GitHub repo.
3. Render -> New -> Web Service -> select repo -> Language: Docker.
4. Add environment variables:
   - WATTWISE_DB_URL      e.g. jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED
   - WATTWISE_DB_USER
   - WATTWISE_DB_PASSWORD
5. Deploy. Tables are created automatically on first start.

Local run: set WATTWISE_DB_PASSWORD, then
  mvn compile exec:java -Dexec.mainClass=com.wattwise.BrowserServer
and open http://localhost:8080
