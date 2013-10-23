package nettyserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import io.netty.handler.codec.http.HttpHeaders.Values;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Accepts HTTP requests and responds to them.
 * @author Gribakin O
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter{
    /**
     * Interval that used on page /hello
     */
    private static final int AWAIT_DURATION_IN_SECONDS = 10;

    private TrafficCounter trafficCounter;    
    
    /**
     * Keeps the count of active channels. 
     */
    private static StatusHolder currentOnline = new StatusHolder();
   
    /**
     * Keeps the map of GET parameters.
     */
    private Map<String, List<String>> parameters;
    /**
     * Keeps the redirect url GET parameter.
     */
    String redirect;
    
    /**
     * Increments a counter of active connections
     * @throws Exception 
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception{        
        currentOnline.add();
        ChannelTrafficShapingHandler t = (ChannelTrafficShapingHandler) ctx.channel().pipeline().get("traffic");                    
        trafficCounter = t.trafficCounter();
        super.channelRegistered(ctx);
    }

    /**
     * Decrement a counter of active connections
     * @throws Exception 
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        currentOnline.remove();
        ctx.channel().close();
        //super.channelInactive(ctx);        
    }
    
    /**
     * Accepts HTTP request and responds by him
     * @throws Exception 
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {                                    
            final HttpRequest req = (HttpRequest) msg;
            final String uri = req.getUri();
            final String src_ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
            final String fullUri = req.headers().get("Host") + uri;
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            String url = decoder.path();
            parameters = decoder.parameters();
            
            if (is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
                        
            redirect = null;
            final FullHttpResponse response;
            final String urlInLower = url.toLowerCase();
            if (urlInLower.matches("/hello")){
                ctx.executor().parent().terminationFuture().await(AWAIT_DURATION_IN_SECONDS, TimeUnit.SECONDS);
                response = createResponse("Hello World", OK);                
            } else if (urlInLower.equals("/redirect")){                
                if (parameters.containsKey("url")) {
                    redirect = parameters.get("url").get(0);
                    response = performRedirect(redirect); 
                } else {
                    response = createErrorResponse("Bad request", BAD_REQUEST);
                }
            } else if (urlInLower.equals("/status")){
                response = performStatus();
            } else {
                response = createErrorResponse("404 Not found", NOT_FOUND);
            }
            
            ChannelFuture writeFuture;                   
            writeFuture = writeResponse(ctx, req, response);            
            writeFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    int received_bytes = (int) trafficCounter.cumulativeReadBytes();
                    int sent_bytes = (int) trafficCounter.cumulativeWrittenBytes();
                    int totalSpeed = (int) (trafficCounter.lastReadThroughput() + trafficCounter.lastWriteThroughput());
                    if (0 == totalSpeed){
                        totalSpeed = sent_bytes + received_bytes; 
                    }
                    trafficCounter.resetCumulativeTime();
                    StatDAO stat = new StatDAO();
                    stat.addLogRecord(src_ip, fullUri, sent_bytes, received_bytes, totalSpeed, redirect);                    
                }              
            });
        } 
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
 

    /**
     * Create HTML page "Status".
     * @return HTTP response, that contains HTML page with status tables.
     */
    private FullHttpResponse performStatus() {
        StatDAO stat = new StatDAO();
        ResultSet resultSetTableIP = null;
        ResultSet resultSetTableRedirect = null;
        ResultSet resultSetTableRecentConnections = null;
        
        // HTML head
        StringBuilder message = new StringBuilder();
        StringBuilder table3 = new StringBuilder();
        message.append("<html><head>");
        message.append("<style type=\"text/css\"> ");
        message.append("table { margin: 1em; border-collapse: collapse; } ");
        message.append("td, th { padding: .3em; border: 1px #ccc solid; } ");
        message.append("thead { background: #A5C5E6; }");
        message.append("p {margin: 1em}");
        message.append("</style></head>");

        // HTML Body
        message.append("<body>");
        try {
            // 1 Total count of requests
            message.append(String.format("<p>Total: %d</p>", stat.getTotalRequests()));
            
            // 2 count of distinct IP
            resultSetTableIP = stat.getTableDataByIP();
            int countByIP = 0;
            
            table3.append("<table>");
            table3.append("<thead><tr><td>IP</td><td>Requests</td><td>Last time</td></tr></thead>");
            while (null != resultSetTableIP && resultSetTableIP.next()) {
                table3.append("<tr><td>").append(resultSetTableIP.getString("URI_STRING")).append("</td><td>");
                table3.append(resultSetTableIP.getString("C")).append("</td><td>");
                table3.append(resultSetTableIP.getString("STAMP")).append("</td></tr>");
                countByIP++;
            }
            table3.append("</table>");
            
            message.append(String.format("<p>Distinct requests by IP: %d</p>", countByIP));
            
            // 3 Count of distinct IP. table with columns | IP | Count of requests from this IP | Date of last Request |
            message.append(table3);            
            
            // 4 Count of redirects. table with columns | IP | Count of requests from this IP | Date of last Request |
            resultSetTableRedirect = stat.getTableDataByRedirect();
            message.append("<table>");
            message.append("<thead><tr><td>URL</td><td>Redirects</td></tr></thead>");
            while (null != resultSetTableRedirect && resultSetTableRedirect.next()) {
                message.append("<tr><td>").append(resultSetTableRedirect.getString("URI_STRING")).append("</td><td>")
                        .append(resultSetTableRedirect.getString("R")).append("</td></tr>");
            }
            message.append("</table>");
            
            // 5 Count of current open connections
            message.append(String.format("<p>Current connections: %d</p>", currentOnline.count()));
            
            // 6 Recent requests, 
            // table with columns | IP | Count of requests from this IP | Date of last Request |
            resultSetTableRecentConnections = stat.getTableDataLast16();
            message.append("<table>");
            message.append("<thead><tr><td>src_ip</td><td>URI</td><td>timestamp</td><td>sent_bytes</td><td"
                   ).append(">received_bytes</td><td>speed (bytes/sec)</td></tr></thead>");
            while (null != resultSetTableRecentConnections && resultSetTableRecentConnections.next()) {
                message.append("<tr><td>").append(resultSetTableRecentConnections.getString("IP")).append("</td><td>");
                message.append(resultSetTableRecentConnections.getString("URI")).append("</td><td>");
                message.append(resultSetTableRecentConnections.getString("STAMP")).append("</td><td>");
                message.append(resultSetTableRecentConnections.getString("SENT_BYTES")).append("</td><td>");
                message.append(resultSetTableRecentConnections.getString("RECEIVED_BYTES")).append("</td><td>");
                message.append(resultSetTableRecentConnections.getString("SPEED")).append("</td></tr>");
            }
            message.append("</table>");   
        } catch (SQLException ex) {
            Logger.getLogger(HttpServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            StatDAO.attemptClose(resultSetTableIP);
            StatDAO.attemptClose(resultSetTableRedirect);
            StatDAO.attemptClose(resultSetTableRecentConnections);            
        }
        message.append("</body></html>");
        return createResponse(message.toString(), OK);
    }

    
    /**
     * Create HTTP response that redirect a client to a new URL.
     * @param newUrl URL to wich need to redirect.
     * @return HTTP response with redirect command.
     */
    private FullHttpResponse performRedirect(String newUrl){
        if (!newUrl.toLowerCase().matches(".*://.*")) {
            newUrl = "http://" + newUrl;
        }
        FullHttpResponse response = createResponse(newUrl, FOUND);
        response.headers().set(LOCATION, newUrl);
        return response;
    }
    
    /**
     * Create simple HTTP response with HTML header to inform client about something
     * @param errorText supporting text
     * @param errorStatus HTTP response status: NOT_FOUND, BAD_REQUEST etc.
     * @return HTTP response with HTML page and error status.
     */
    private FullHttpResponse createErrorResponse(String errorText, HttpResponseStatus errorStatus) {
        String message = "<html><body><h3>" + errorText + "</h3></body></html>";        
        return createResponse(message, errorStatus);
    }
    
    /**
     * Create HTTP response with the specified message and status
     * @param message Body of the response. HTML page or plain text.
     * @param status HTTP response status: NOT_FOUND, BAD_REQUEST etc.
     * @return HTTP response 
     */
    private FullHttpResponse createResponse(String message, HttpResponseStatus status) {
        ByteBuf content = Unpooled.wrappedBuffer(message.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        response.headers().set(CONTENT_TYPE, "text/html; charset=utf-8");        
        return response;
    }
    
    /**
    * Sends a finished HTTP response to client.
    * @param response 
    * @return The result of an asynchronous channel I/O operation.
    */
    private ChannelFuture writeResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse response) throws InterruptedException {
        response.headers().set(CONTENT_LENGTH, response.content().capacity());
        boolean keepAlive = isKeepAlive(req);
        if (!keepAlive) {
            return ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            return ctx.writeAndFlush(response);
        }
    }   
    
}
