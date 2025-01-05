package org.netty_dev.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SrvChFileHandler extends Http3RequestStreamInboundHandler {

    private int filesize = 0;
    private  int fs=0;
    private ArrayList<byte[]> chunks = new ArrayList<>();

    public SrvChFileHandler() {
        System.out.println(this.hashCode());
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        filesize = Integer.parseInt( frame.headers().get("content-length").toString());
        System.out.println("File size: " + filesize);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("Received chunk of size: " + frame.content().readableBytes());

        fs= fs+ frame.content().readableBytes();
        ByteBuf byteBuf = frame.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        chunks.add(bytes);
        System.out.println("Received: " + fs + " bytes");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        String outputFilePath = "C:\\Users\\vasil\\OneDrive\\Рабочий стол\\Новая папка\\Точечный рисунок.jpg"; // Укажите путь для результирующего файла


        System.out.println("File size: " + filesize);
        System.out.println(chunks.size());
        System.out.println(chunks);
        try {
            // Собираем файл из чанков
            assembleFileFromChunks(chunks, outputFilePath);
            System.out.println("File has been successfully created: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
        System.out.println("File read completed");

    }

    public static void assembleFileFromChunks(ArrayList<byte[]> chunks, String outputFilePath) throws IOException {
        File outputFile = new File(outputFilePath);

        // Убедимся, что родительская директория существует
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            for (byte[] chunk : chunks) {
                outputStream.write(chunk);
            }
        }
    }
}
