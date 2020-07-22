%YAML 1.2
---

#===============================================================================
#                     IAST Default Hook Definition
# -----------------------------------------------------------------------------
# Author    shiyan s00383986
# Date:        2019-10-15
# Copyright:   Security Tool Dev Department / Cloud & AI /
#                 Huawei Technologies Co., Ltd.
#===============================================================================


# ------------------------------container hook--------------------------------
AsyncJettyContextChanger:
  desc: "async jetty hook"
  hook_classes: org.eclipse.jetty.server.AsyncContextState
  hook_points:
    - methods: getRequest
      insert_after:
        invoke_handlers:
          handler: RecordServletRequestHandler
          parameters: state().getAsyncContextEvent().getSuppliedRequest()
    - methods: complete
      insert_before:
        invoke_handlers:
          handler: RecordServletResponseHandler
          parameters: this.getResponse()
      insert_after:
        invoke_handlers:
          handler: RequestFinishHandler


AsyncJettyContextEventChanger:
  hook_classes: org.eclipse.jetty.server.AsyncContextEvent
  hook_points:
    - constructors: >
        (org.eclipse.jetty.server.handler.ContextHandler$Context, org.eclipse.jetty.server.AsyncContextState,
        org.eclipse.jetty.server.Request,javax.servlet.ServletRequest, javax.servlet.ServletResponse)
      insert_before:
        write_raw_code: 
          code: request=%stub%
          stub: 
            handler: WrapAndRecordAsyncServletRequestHandler
            parameters: request


AsyncTomcatContextChanger:
  hook_classes: org.apache.catalina.core.AsyncContextImpl
  hook_points:
    - methods: getRequest
      insert_after:
        invoke_handlers:
          handler: RecordServletRequestHandler
          parameters: servletRequest
    - methods: complete
      insert_before:
        invoke_handlers:
          - handler: RecordServletResponseHandler
            parameters: this.getResponse()
          - handler: RequestFinishHandler


AsyncTomcatStartSyncChanger:
  hook_for: FILE_UPLOAD
  hook_classes: org.apache.catalina.connector.Request
  hook_points:
    - methods: 'javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest,javax.servlet.ServletResponse)'
      replace_method:
        target_method: setStarted
        write_raw_code:
          code: >
            javax.servlet.http.HttpServletRequest newRequest = (javax.servlet.http.HttpServletRequest) %stub%;
            $proceed(getContext(), newRequest, response, request==getRequest() && response==getResponse().getResponse());
          stub: 
            handler: WrapAndRecordAsyncServletRequestHandler
            parameters: $2
    - methods: getParts
      insert_after:
        invoke_handlers:
          handler: ServletPartsUploadHandler
          parameters: $_


HttpServletChanger:
  enable: false
  hook_classes: javax.servlet.http.HttpServlet
  hook_points:
    - methods: 'void service(javax/servlet/ServletRequest,javax/servlet/ServletResponse)'
      replace_method:
        target_method: service
        write_raw_code:
          code: >
            javax.servlet.http.HttpServletRequest newRequest = (javax.servlet.http.HttpServletRequest) %stub1%;
            $proceed(newRequest, $2);
            %stub2%;
            %stub3%;
          stub1: 
            handler: WrapAndRecordServletRequestHandler
            parameters: $1
          stub2: 
            handler: RecordServletResponseHandler
            parameters: $2
          stub3: 
            handler: RequestFinishHandler


JettyServletPartsUploadChanger:
  hook_for: FILE_UPLOAD
  hook_classes: org.eclipse.jetty.server.Request
  hook_points:
    - methods: getParts
      insert_after:
        invoke_handlers:
          handler: ServletPartsUploadHandler
          parameters: $_


JettyFilterChainInvokerChanger:
  hook_classes: org.eclipse.jetty.servlet.ServletHandler
  hook_points:
    - methods: doHandle
      replace_method:
        - target_method: Chain.doFilter
          write_raw_code:
            code: >
              javax.servlet.http.HttpServletRequest newRequest = (javax.servlet.http.HttpServletRequest) %stub1%;
              $proceed(newRequest, $2);
              %stub2%;
              %stub3%;
            stub1: 
              handler: WrapAndRecordServletRequestHandler
              parameters: $1
            stub2: 
              handler: RecordServletResponseHandler
              parameters: $2
            stub3: 
              handler: RequestFinishHandler
        - target_method: ServletHolder.handle
          write_raw_code:
            code: >
              javax.servlet.http.HttpServletRequest newRequest = (javax.servlet.http.HttpServletRequest) %stub1%;
              $proceed($1, newRequest, $3);
              %stub2%;
              %stub3%;
            stub1: 
              handler: WrapAndRecordServletRequestHandler
              parameters: $2
            stub2: 
              handler: RecordServletResponseHandler
              parameters: $3
            stub3: 
              handler: RequestFinishHandler


TomcatFilterChainInvokerChanger:
  hook_classes: org.apache.catalina.core.StandardWrapperValve
  hook_points:
    - methods: invoke
      replace_method:
        target_method: FilterChain.doFilter
        write_raw_code:
          code: >
            javax.servlet.http.HttpServletRequest newRequest = (javax.servlet.http.HttpServletRequest) %stub1%;
            $proceed(newRequest, $2);
            %stub2%;
            %stub3%;
          stub1: 
            handler: WrapAndRecordServletRequestHandler
            parameters: $1
          stub2: 
            handler: RecordServletResponseHandler
            parameters: $2
          stub3: 
            handler: RequestFinishHandler


VertxRestInvocationChanger:
  hook_classes: org.apache.servicecomb.common.rest.AbstractRestInvocation
  hook_points:
    - methods: invoke
      insert_before:
        invoke_handlers:
          handler: RecordVertxRequestHandler
          parameters: requestEx
      insert_after:
        invoke_handlers:
          - handler: RecordVertxResponseHandler
            parameters: responseEx
          - handler: RequestFinishHandler


HttpServletResponseChanger:
  hook_for:
    - URL_REDIRECT_INJECTION
    - CRLF_INJECTION
  hook_classes:
    - org.apache.catalina.connector.ResponseFacade
    - org.eclipse.jetty.server.Response
  hook_points:
    - methods: 
        - 'void setHeader(String, String)'
        - 'void addHeader(String, String)'
      insert_before:
        invoke_handlers:
          handler: CrlfInjHandler
          parameters: $2
    - methods: 'void sendRedirect(String)'
      insert_before:
        invoke_handlers:
          handler: UrlRedirectHandler
          parameters: $1

# ---------------------------- deserialize hook ---------------------------------
ObjectInputStreamChanger:
  desc: 'java
  hook_for: JAVA_DESERIALIZED
  hook_classes: java/io/ObjectInputStream
  hook_points:
    - methods: 
        - readObject
        - read
      insert_after:
        invoke_handlers:
          handler: JavaNativeDeserializeHandler
          parameters: this


SnakeyamlChanger:
  hook_for: YAML_DESERIALIZED
  hook_classes: org/yaml/snakeyaml/Yaml
  hook_points:
    - constructors:
        - (org.yaml.snakeyaml.constructor.BaseConstructor, org.yaml.snakeyaml.representer.Representer, org.yaml.snakeyaml.DumperOptions, org.yaml.snakeyaml.LoaderOptions, org.yaml.snakeyaml.resolver.Resolver)
        - (org.yaml.snakeyaml.constructor.BaseConstructor, org.yaml.snakeyaml.representer.Representer, org.yaml.snakeyaml.DumperOptions, org.yaml.snakeyaml.resolver.Resolver)
      insert_after:
        invoke_handlers:
          handler: SnakeyamlHandler
          parameters: $1.getClass().getSimpleName()


FastJsonChanger:
  hook_for: JSON_DESERIALIZED
  hook_classes: com/alibaba/fastjson/parser/DefaultJSONParser
  hook_points:
    - methods: void handleResovleTask(Object)
      insert_before:
        invoke_handlers: 
          handler: FastJsonDeserializeHandler
          parameters: this,$1


JacksonChanger:
  hook_for: JSON_DESERIALIZED
  hook_classes: com/fasterxml/jackson/databind/ObjectMapper
  hook_points:
    - methods: 
        - _readMapAndClose
        - _readValue
        - _readTreeAndClose
      insert_after:
        invoke_handlers: 
          handler: JacksonDeserializeHandler
          parameters: this,$_


JsonIOChanger:
  hook_for: JSON_DESERIALIZED
  hook_classes: com/cedarsoftware/util/io/JsonReader
  hook_points:
    - methods: readObject
      insert_after:
        invoke_handlers:
          handler: JsonIODeserializeHandler
          parameters: this,$_


XMLDecoderChanger:
  hook_for: XML_DESERIALIZED
  hook_classes: java/beans/XMLDecoder
  hook_points:
    - methods: readObject
      insert_after:
        invoke_handlers:
          handler: XMLDecoderHandler
          parameters: $_


XStreamChanger:
  hook_for: XML_DESERIALIZED
  hook_classes: com/thoughtworks/xstream/XStream
  hook_points:
    - methods: Object unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, Object, com.thoughtworks.xstream.converters.DataHolder)
      insert_after:
        invoke_handlers:
          handler: XStreamHandler
          parameters: this,$_

KryoChanger:
  hook_for: BINARY_DESERIALIZED
  hook_classes: com/esotericsoftware/kryo/Kryo
  hook_points:
    - methods:
        - readClass
        - readReferenceOrNull
      insert_after:
        invoke_handlers:
          handler: KryoHandler
          parameters: this

# ---------------------------- httpclient hook ---------------------------------

ApacheHttpClientChanger:
  hook_for: SSRF
  hook_classes: org/apache/http/impl/client/InternalHttpClient
  hook_points:
    - methods: org.apache.http.client.methods.CloseableHttpResponse doExecute(org.apache.http.HttpHost, org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)
      insert_after: 
        invoke_handlers:
          handler: SSRFInternalHttpClientHandler
          parameters: $2


FileClientChanger:
  enable: false
  hook_for: SSRF
  hook_classes: sun/net/www/protocol/file/FileURLConnection
  hook_points:
    - constructors: (java.net.URL, java.io.File)
      insert_before:
        invoke_handlers:
          handler: SSRFHttpsURLConnectionHandler
          parameters: $1


JavaHttpClientChanger:
  hook_for: SSRF
  hook_classes: sun/net/www/protocol/http/HttpURLConnection
  hook_points:
    - constructors: (java.net.URL, java.net.Proxy, sun.net.www.protocol.http.Handler)
      insert_before:
        invoke_handlers:
          handler: SSRFHttpsURLConnectionHandler
          parameters: $1


JavaHttpsClientChanger:
  hook_for: SSRF
  hook_classes: sun/net/www/protocol/https/HttpsURLConnectionImpl
  hook_points:
    - constructors: (java.net.URL, java.net.Proxy, sun.net.www.protocol.https.Handler)
      insert_before:
        invoke_handlers:
          handler: SSRFHttpsURLConnectionHandler
          parameters: $1

# ---------------------------- io hook ---------------------------------
ApacheItemsUploadChanger:
  hook_for: FILE_UPLOAD
  hook_classes: org.apache.commons.fileupload.FileUploadBase
  hook_points:
    - methods: java.util.List parseRequest(org.apache.commons.fileupload.RequestContext)
      insert_after:
        invoke_handlers:
          handler: ApacheItemsUploadHandler
          parameters: $_

ApacheFileItemChanger:
  hook_for: FILE_UPLOAD
  hook_classes:
    - org.apache.tomcat.util.http.fileupload.disk.DiskFileItem
    - org.apache.commons.fileupload.disk.DiskFileItem
  hook_points:
    - methods: void write(java.io.File)
      insert_after:
        invoke_handlers:
          handler: FilePartOutPutStreamHandler
          parameters: $1.getPath() , get()

JettyPartsUploadChanger:
  hook_for: FILE_UPLOAD
  hook_classes: org.eclipse.jetty.http.MultiPartFormInputStream$MultiPart
  hook_points:
    - methods: void write(String)
      insert_after:
        invoke_handlers:
          handler: FilePartOutPutStreamHandler
          parameters: $1, getBytes()

FileOutputStreamChanger:
  hook_for:
    - FILE_COVERAGE
  hook_classes: java/io/FileOutputStream
  hook_points:
    - constructors: (java.io.File, boolean)
      insert_before:
        invoke_handlers:
          - handler: FileCoverageHandler
            parameters: $1
          - handler: InputStreamPathTravelHandler
            parameters: $1


FileInputStreamChanger:
  hook_for: PATH_TRAVERSAL
  hook_classes: java/io/FileInputStream
  hook_points:
    - constructors: (java.io.File)
      insert_before:
        invoke_handlers:
          handler: InputStreamPathTravelHandler
          parameters: $1


RandomFileChanger:
  hook_for: PATH_TRAVERSAL
  hook_classes: java/io/RandomAccessFile
  hook_points:
    - constructors: (java.io.File, String)
      insert_after:
        invoke_handlers:
          handler: InputStreamPathTravelHandler
          parameters: $1


FileChanger:
  enable: false
  hook_for: PATH_TRAVERSAL
  hook_classes: java/io/File
  hook_points:
    - constructors:
        - (String)
        - (String, String)
        - (java.io.File, String)
        - (java.net.URI)
      insert_after:
        invoke_handlers:
          handler: FilePathTravelHandler
          parameters: path


FilePathsChanger:
  enable: false
  hook_for: PATH_TRAVERSAL
  hook_classes: java/nio/file/Paths
  hook_points:
    - methods:
        - java.nio.file.Path get(String, String[])
        - java.nio.file.Path get(java.net.URI)
      insert_after:
        invoke_handlers:
          handler: PathsPathTravelHandler
          parameters: $_

FileFilesChanger:
  hook_for: PATH_TRAVERSAL
  hook_classes: java/nio/file/Files
  hook_points:
    - methods:
        - java.io.InputStream newInputStream(java.nio.file.Path, java.nio.file.OpenOption[])
      insert_before:
        invoke_handlers:
          handler: PathsPathTravelHandler
          parameters: $1




# ---------------------------- jdbc hook ---------------------------------

JDBCReflectConnectionChanger:
  hook_for:
    - SQL_INJECTION
    - XSS_STORE
    - DB_INFORMATION_LEAK
  hook_classes:
    type: reflect
    condition:
      implements: java.sql.Connection
  hook_points:
    - methods:
        - java.sql.PreparedStatement prepareStatement(String)
        - java.sql.PreparedStatement prepareStatement(String, int)
        - java.sql.PreparedStatement prepareStatement(String, int, int)
        - java.sql.PreparedStatement prepareStatement(String, int, int, int)
        - java.sql.PreparedStatement prepareStatement(String, int[])
        - java.sql.PreparedStatement prepareStatement(String, String[])
        - java.sql.CallableStatement prepareCall(String)
        - java.sql.CallableStatement prepareCall(String, int, int)
        - java.sql.CallableStatement prepareCall(String, int, int, int)
      insert_before:
        invoke_handlers:
          - handler: SQLInjHandler
            parameters: $1
          - handler: XssStoreHandler
            parameters: $1
          - handler: DbInfoLeakHandler
            parameters: $1


JDBCConnectionChanger:
  desc: 
  hook_for:
    - SQL_INJECTION
    - XSS_STORE
    - DB_INFORMATION_LEAK
  hook_classes: 
    - com/mysql/jdbc/ConnectionImpl
    - com/mysql/cj/jdbc/ConnectionImpl
    - com/microsoft/sqlserver/jdbc/SQLServerConnection
    - oracle/jdbc/driver/PhysicalConnection
    - com/huawei/teastore/jdbc/impl/JDBCConnection
    - org/postgresql/jdbc4/Jdbc4Connection
    - org/postgresql/jdbc/PgConnection
    - com/sybase/jdbc3/jdbc/SybConnection
    - com/ibm/db2/jcc/am/Connection
    - org/sqlite/jdbc4/JDBC4Connection
    - org/hsqldb/jdbc/jdbcConnection
    - org/hsqldb/jdbc/JDBCConnection
    - com/huawei/gauss/jdbc/inner/GaussStatementImpl/GaussConnectionImpl
  hook_points:
    - methods: 
        - java.sql.PreparedStatement prepareStatement(String)
        - java.sql.PreparedStatement prepareStatement(String, int)
        - java.sql.PreparedStatement prepareStatement(String, int, int)
        - java.sql.PreparedStatement prepareStatement(String, int, int, int)
        - java.sql.PreparedStatement prepareStatement(String, int[])
        - java.sql.PreparedStatement prepareStatement(String, String[])
        - java.sql.CallableStatement prepareCall(String)
        - java.sql.CallableStatement prepareCall(String, int, int)
        - java.sql.CallableStatement prepareCall(String, int, int, int)
      insert_before:
        invoke_handlers:
          - handler: SQLInjHandler
            parameters: $1
          - handler: XssStoreHandler
            parameters: $1
          - handler: DbInfoLeakHandler
            parameters: $1

JDBCReflectPrepareStatementChanger:
  hook_for:
    - XSS_STORE
    - DDE_INJECT
    - DB_INFORMATION_LEAK
  hook_classes:
    type: reflect
    condition:
      implements: java.sql.PreparedStatement
  hook_points:
    - methods: void setString(int, String)
      insert_before:
        invoke_handlers:
          - handler: XssStoreHandler
            parameters: $2
          - handler: DDEInjHandler
            parameters: $2
          - handler: DbInfoLeakHandler  # 
            parameters: $2

JDBCPrepareStatementChanger:
  hook_for:
    - XSS_STORE
    - DDE_INJECT
    - DB_INFORMATION_LEAK
  hook_classes:
    - com/mysql/jdbc/PreparedStatement
    - com/mysql/cj/jdbc/PreparedStatement
    - com/microsoft/sqlserver/jdbc/SQLServerPreparedStatement
    - oracle/jdbc/driver/OraclePreparedStatement
    - org/postgresql/jdbc/PgPreparedStatement
    - com/huawei/teastore/jdbc/impl/JDBCPreparedStatement
    - com/sybase/jdbc3/jdbc/SybPreparedStatement
    - com/ibm/db2/jcc/am/uo
    - org/sqlite/jdbc3/JDBC3PreparedStatement
    - org/hsqldb/jdbc/jdbcPreparedStatement
    - org/hsqldb/jdbc/JDBCPreparedStatement
    - com/huawei/gauss/jdbc/inner/GaussStatementImpl/GaussPrepareStmtImpl
  hook_points:
    - methods: void setString(int, String)
      insert_before:
        invoke_handlers:
          - handler: XssStoreHandler
            parameters: $2
          - handler: DDEInjHandler
            parameters: $2
          - handler: DbInfoLeakHandler  # 
            parameters: $2


JDBCReflectStatementChanger:
  hook_for:
    - SQL_INJECTION
    - DDE_INJECT
    - XSS_STORE
    - DB_INFORMATION_LEAK
  hook_classes:
    type: reflect
    condition:
      implements: java.sql.Statement
  hook_points:
    - methods:
        - java.sql.ResultSet executeQuery(String)
        - boolean execute(String)
        - boolean execute(String, int)
        - boolean execute(String, int[])
        - boolean execute(String, String[])
        - void addBatch(String)
        - int executeUpdate(String)
        - int executeUpdate(String, int)
        - int executeUpdate(String, int[])
        - int executeUpdate(String, String[])
        - long executeLargeUpdate(String)
        - long executeLargeUpdate(String, int)
        - long executeLargeUpdate(String, int[])
        - long executeLargeUpdate(String, String[])
      insert_before:
        invoke_handlers:
          - handler: SQLInjHandler
            parameters: $1
          - handler: XssStoreHandler
            parameters: $1
          - handler: DDEInjHandler
            parameters: $1
          - handler: DbInfoLeakHandler
            parameters: $1

JDBCStatementChanger:
  hook_for:
    - SQL_INJECTION
    - DDE_INJECT
    - XSS_STORE
    - DB_INFORMATION_LEAK
  hook_classes:
    - com/mysql/jdbc/StatementImpl
    - com/mysql/cj/jdbc/StatementImpl
    - com/microsoft/sqlserver/jdbc/SQLServerStatement
    - oracle/jdbc/driver/OracleStatement
    - com/huawei/teastore/jdbc/impl/JDBCStatement
    - org/postgresql/jdbc2/AbstractJdbc2Statement
    - org/postgresql/jdbc/PgStatement
    - com/sybase/jdbc3/jdbc/SybStatement
    - com/ibm/db2/jcc/am/to
    - org/sqlite/jdbc3/JDBC3Statement
    - org/hsqldb/jdbc/jdbcStatement
    - org/hsqldb/jdbc/JDBCStatement
    - com/huawei/gauss/jdbc/inner/GaussStatementImpl
  hook_points:
    - methods: 
        - java.sql.ResultSet executeQuery(String)
        - boolean execute(String)
        - boolean execute(String, int)
        - boolean execute(String, int[])
        - boolean execute(String, String[])
        - void addBatch(String)
        - int executeUpdate(String)
        - int executeUpdate(String, int)
        - int executeUpdate(String, int[])
        - int executeUpdate(String, String[])
        - long executeLargeUpdate(String)
        - long executeLargeUpdate(String, int)
        - long executeLargeUpdate(String, int[])
        - long executeLargeUpdate(String, String[])
      insert_before:
        invoke_handlers:
          - handler: SQLInjHandler
            parameters: $1
          - handler: XssStoreHandler
            parameters: $1
          - handler: DDEInjHandler
            parameters: $1
          - handler: DbInfoLeakHandler
            parameters: $1


# ---------------------------- log hook ---------------------------------

JDKLoggerChanger:
  enable: false
  hook_for:
    - LOG_INJECTION
    - INFORMATION_LEAK
  hook_classes: java/util/logging/FileHandler
  hook_points:
    - methods: void publish(java.util.logging.LogRecord)
      replace_method:
        target_method: publish
        write_raw_code:
          code: >
            %stub1%;
            %stub2%;
            $proceed($1);
          stub1: 
            handler: LogInfoLeakHandler
            parameters: $1.getMessage()
          stub2: 
            handler: LogInjHandler
            parameters: $1.getMessage()


Log4jAppenderChanger:
  hook_for: 
    - LOG_INJECTION
    - INFORMATION_LEAK
  hook_classes: org/apache/log4j/WriterAppender
  hook_points:
    - methods: void subAppend(org.apache.log4j.spi.LoggingEvent)
      insert_before:
        invoke_handlers:
          - handler: LogInfoLeakHandler
            parameters: $1.getMessage()
          - handler: LogInjHandler
            parameters: $1.getMessage()


Log4j2AppenderControlChanger:
  hook_for: 
    - LOG_INJECTION
    - INFORMATION_LEAK
  hook_classes: org/apache/logging/log4j/core/config/AppenderControl
  hook_points:
    - methods: void tryCallAppender(org.apache.logging.log4j.core.LogEvent)
      insert_before:
        invoke_handlers:
          - handler: LogInfoLeakHandler
            parameters: $1.getMessage().getFormattedMessage()
          - handler: LogInjHandler
            parameters: $1.getMessage().getFormattedMessage()


LogbackAppenderBaseChanger:
  hook_for:
    - LOG_INJECTION
    - INFORMATION_LEAK
  hook_classes: ch/qos/logback/core/OutputStreamAppender
  hook_points:
    - methods: subAppend
      insert_before:
        invoke_handlers:
          - handler: LogInfoLeakHandler
            parameters: ((ch.qos.logback.classic.spi.LoggingEvent)$1).getMessage()
          - handler: LogInjHandler
            parameters: ((ch.qos.logback.classic.spi.LoggingEvent)$1).getMessage()


SafeLog:
  hook_classes: com.huawei.csb.customerorg.common.utils.log.CBCPatternConverter
  hook_points:
    - methods: convert
      insert_before:
        invoke_handlers:
          - handler: SafeLogUsageHandler


# ---------------------------- xml hook ---------------------------------

DomChanger:
  hook_for: XXE
  hook_classes:
    - com/sun/org/apache/xerces/internal/parsers/DOMParser
    - org/apache/xerces/parsers/DOMParser
  hook_points:
    - methods: void parse(org.xml.sax.InputSource)
      insert_before:
        invoke_handlers:
          handler: XXEHandler
          parameters: this


XMLSAXChanger:
  hook_for: XXE
  hook_classes:
    - com/sun/org/apache/xerces/internal/jaxp/SAXParserImpl$JAXPSAXParser
    - org/apache/xerces/jaxp/SAXParserImpl$JAXPSAXParser
    - org/apache/xerces/parsers/SAXParser
  hook_points:
    - methods: 
        - void parse(org.xml.sax.InputSource)
        - void parse(String)
      insert_before:
        invoke_handlers:
          handler: XXEHandler
          parameters: this


XMLStAXChanger:
  hook_for: XXE
  hook_classes: com.sun.xml.internal.stream.XMLInputFactoryImpl
  hook_points:
    - methods: javax.xml.stream.XMLStreamReader getXMLStreamReaderImpl(com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource)
      insert_before:
        invoke_handlers:
          handler: XXEHandler
          parameters: this


XMLValidatorChanger:
  hook_for: XXE
  hook_classes: 
    - com/sun/org/apache/xerces/internal/jaxp/validation/ValidatorImpl
    - org/apache/xerces/jaxp/validation/ValidatorImpl
  hook_points:
    - methods: validate
      insert_before:
        invoke_handlers:
          handler: XXEHandler
          parameters: this

# ---------------------------- xpath hook ---------------------------------

Dom4jXPathChanger:
  hook_for: XPATH_INJECTION
  hook_classes: org/dom4j/xpath/DefaultXPath
  hook_points:
    - methods: void setNSContext(Object)
      insert_after:
        invoke_handlers:
          handler: XPathInjHandler
          parameters: xpath


JaxpXPathChanger:
  hook_for: XPATH_INJECTION
  hook_classes:
    - com/sun/org/apache/xpath/internal/jaxp/XPathImpl
    - org/apache/xpath/jaxp/XPathImpl
  hook_points:
    - methods:
        - javax.xml.xpath.XPathExpression compile(String)
        - Object evaluate(String, org.xml.sax.InputSource, javax.xml.namespace.QName)
        - Object evaluate(String, Object, javax.xml.namespace.QName)
      insert_before:
        invoke_handlers:
          handler: XPathInjHandler
          parameters: $1

# ---------------------------- other hook ---------------------------------

LDAPInitialContextChanger:
  hook_for: LDAP_INJECTION
  hook_classes:
    - javax/naming/directory/InitialDirContext
  hook_points:
    - methods:
        - javax.naming.NamingEnumeration search(String, String, javax.naming.directory.SearchControls)
        - javax.naming.NamingEnumeration search(String, String, Object[], javax.naming.directory.SearchControls)
        - javax.naming.NamingEnumeration search(javax.naming.Name, String, javax.naming.directory.SearchControls)
        - javax.naming.NamingEnumeration search(javax.naming.Name, String, Object[], javax.naming.directory.SearchControls)
      insert_before:
        invoke_handlers:
          handler: LDAPInjHandler
          parameters: $2


OgnlChanger:
  hook_for: OGNL_INJECTION
  hook_classes: ognl/Ognl
  hook_points:
    - methods: Object parseExpression(String)
      insert_after:
        invoke_handlers:
          handler: OgnlInjHandler
          parameters: $1


SpelChanger:
  hook_for: SPEL_INJECTION
  hook_classes: org.springframework.expression.spel.standard.InternalSpelExpressionParser
  hook_points:
    - methods: doParseExpression
      insert_before:
        invoke_handlers:
          handler: SpelInjHandler
          parameters: $1,$2


ChannelExecChanger:
  hook_for: COMMAND_INJECTION
  hook_classes: com/jcraft/jsch/ChannelExec
  hook_points:
    - methods: void setCommand(String)
      insert_after:
        invoke_handlers:
          handler: CmdInjHandler
          parameters: $1


ProcessBuilderChanger:
  hook_for:
    - COMMAND_INJECTION
  hook_classes: java/lang/ProcessBuilder
  hook_points:
    - methods: start
      insert_after:
        invoke_handlers:
          handler: CmdInjHandler
          parameters: command,environment


JavaHashChanger:
  hook_for: BM_WEAK_HASH
  hook_classes: java/security/MessageDigest
  hook_points:
    - methods:
        - java.security.MessageDigest getInstance(String)
        - java.security.MessageDigest getInstance(String, String)
        - java.security.MessageDigest getInstance(String, java.security.Provider)
      insert_before:
        invoke_handlers:
          handler: JavaHashHandler
          parameters: $1


JavaxCryptoChanger:
  hook_for: BM_WEAK_CRYPTO
  hook_classes: javax/crypto/Cipher
  hook_points:
    - methods:
        - javax.crypto.Cipher getInstance(String)
        - javax.crypto.Cipher getInstance(String, java.security.Provider)
      insert_before:
        invoke_handlers:
          handler: JavaxCryptoHandler
          parameters: $1
