package nettyserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Turns on a netty-based server
 * @author Gribakin O
 */
public class NettyServer implements Runnable {
    private Logger logger = Logger.getLogger(NettyServer.class.getName());
    
    public static final int DEFAULT_PORT = 3000;
    private int port;
    private ChannelFuture cf = null;
    
    public NettyServer(int port){
        this.port = port;
    }
    
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        EventLoopGroup workerGroup = new NioEventLoopGroup();                
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) 
             .childHandler(new HttpServerInitializer())             
             .option(ChannelOption.SO_BACKLOG, 512);               
            
            Channel ch = bootstrap.bind(port).sync().channel();
            System.out.println("Web server started at port " + port + '.');
            printHelp();
            Database.getConnection();
            cf = ch.closeFuture();
            cf.sync();
            Database.closeConnection();
        } catch (SQLException ex){
            logger.severe(ex.getMessage());
        } catch (InterruptedException ex){
            logger.severe(ex.getMessage());
        } catch (Exception ex){            
            if (ex.getMessage().startsWith("Address already in use")){
                System.out.println("Port " + port + " is already in use");
                System.out.println("To specify another port run server with command \"NettyServer port\"");
                System.exit(0);
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();         
        }
    }
    
    /**
     * Shuts down the server.
     */
    public void stop(){        
        cf.channel().close();                
    }
    
    public static void main(String[] args) throws Exception{
        int port;
        if (args.length > 0){
            port = Integer.parseInt(args[0]);
        } else {
            port = DEFAULT_PORT;
        }
        NettyServer server = new NettyServer(port);
        Thread serverThread = new Thread((Runnable) server, "main"); 
        
        serverThread.start();
        
        Scanner in = new Scanner(System.in);
        Boolean stop = false;
        while (!stop){ 
            if (in.hasNext()){
                if ("stop".equalsIgnoreCase(in.next())){
                    stop = true;
                } else if ("sql-tool".equalsIgnoreCase(in.next())) {
                    ManualDBForm sqlForm = new ManualDBForm();
                    sqlForm.setVisible(true);
                } else {
                    printHelp();
                }
            }                     
        }        
        server.stop();            
    }
    
    /**
     * Prints the list of commands to console.
     */
    private static void printHelp(){
        System.out.println("type \"stop\" to stop server");
    }
}



