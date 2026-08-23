# Copyright (c) 2026 weblogic-todo-jsf local domain helpers.
import os

domain_name = os.environ.get('DOMAIN_NAME', 'todo_domain')
admin_name = os.environ.get('ADMIN_NAME', 'AdminServer')
domain_path = '/u01/oracle/user_projects/domains/%s' % domain_name
db_url = os.environ.get('DB_URL', 'jdbc:oracle:thin:@//db:1521/FREEPDB1')
db_user = os.environ.get('DB_USER', 'todo')
db_password = os.environ.get('DB_PASSWORD', 'TodoPassword1')

print('Configuring JDBC TodoDS')
print('domain_path : [%s]' % domain_path)
print('db_url      : [%s]' % db_url)
print('db_user     : [%s]' % db_user)

readDomain(domain_path)

try:
    cd('/JDBCSystemResource/TodoDS')
    print('TodoDS already exists, skipping create')
    closeDomain()
    exit()
except:
    pass

cd('/')
create('TodoDS', 'JDBCSystemResource')
cd('/JDBCSystemResource/TodoDS')
set('Target', admin_name)

cd('/JDBCSystemResource/TodoDS/JdbcResource/TodoDS')
cmo.setName('TodoDS')

cd('/JDBCSystemResource/TodoDS/JdbcResource/TodoDS')
create('TodoDriverParams', 'JDBCDriverParams')
cd('JDBCDriverParams/NO_NAME_0')
set('DriverName', 'oracle.jdbc.OracleDriver')
set('URL', db_url)
set('PasswordEncrypted', db_password)
set('UseXADataSourceInterface', 'false')

create('TodoDriverProperties', 'Properties')
cd('Properties/NO_NAME_0')
create('user', 'Property')
cd('Property/user')
set('Value', db_user)

cd('/JDBCSystemResource/TodoDS/JdbcResource/TodoDS')
create('TodoDSParams', 'JDBCDataSourceParams')
cd('JDBCDataSourceParams/NO_NAME_0')
set('JNDIName', jarray.array([String('jdbc/TodoDS')], String))
set('GlobalTransactionsProtocol', 'OnePhaseCommit')

cd('/JDBCSystemResource/TodoDS/JdbcResource/TodoDS')
create('TodoPoolParams', 'JDBCConnectionPoolParams')
cd('JDBCConnectionPoolParams/NO_NAME_0')
set('TestTableName', 'SQL ISVALID')
set('TestConnectionsOnReserve', true)
set('InitialCapacity', 1)
set('MaxCapacity', 10)
set('ConnectionReserveTimeoutSeconds', 30)

updateDomain()
closeDomain()
print('TodoDS created')
exit()
