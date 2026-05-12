#!/bin/sh

cat > /opt/payara/deployments/setup.pyara << EOF
create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGSimpleDataSource --restype javax.sql.DataSource --property serverName=${DB_HOST}:portNumber=${DB_PORT}:databaseName=${DB_NAME}:user=${DB_USER}:password=${DB_PASSWORD} folder_mgmt_pool
create-jdbc-resource --connectionpoolid folder_mgmt_pool jdbc/CloudDrivePu
EOF

# تشغيل Payara
exec java -jar /opt/payara/payara-micro.jar \
    --postbootcommandfile /opt/payara/deployments/setup.pyara \
    --deploy /opt/payara/deployments/app.war \
    --nocluster