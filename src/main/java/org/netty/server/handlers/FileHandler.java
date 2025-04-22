package org.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;
import org.netty.server.Route;
import org.netty.server.ServerParams;
import org.netty.server.authorization.Authorization;

import java.io.File;

@Route(route = "/secure/files/")
public class FileHandler extends Http3RequestStreamInboundHandler {

    private String mainDirectory = ServerParams.FILESTORAGEDIRECTORY;

    private File workingDirectory;

    private String username;
    private String role;

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        username = Authorization.JwtUtil.getJWTusername(frame.headers().get("authorization").toString().split(" ")[1]);
        role = Authorization.JwtUtil.getJWTrole(frame.headers().get("authorization").toString().split(" ")[1]);

        System.out.println("username: " + username);
        System.out.println("role: " + role);

        if (role.equals("admin")) {
            // showing all directories
        }

        if (role.equals("user")) {
            // showing only user's directories
            mainDirectory = mainDirectory + "\\" + username;
            workingDirectory = new File(mainDirectory);

            if (!workingDirectory.exists()) {
                workingDirectory.mkdir();
            }
        }
        for(File f: workingDirectory.listFiles()) {
            System.out.println(f.getName());
        }

    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {

    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }
}
