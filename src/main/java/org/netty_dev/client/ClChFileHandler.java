package org.netty_dev.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.util.NetUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ClChFileHandler extends Http3RequestStreamInboundHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        String filePath = "C:\\Users\\vasil\\Downloads\\IMG_20241125_102058.jpg"; // Укажите путь к вашему файлу
        int chunkSize = 1*1024*1024; // Размер чанка в байтах // 1 chunk - 1MB





        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
             FileChannel fileChannel = file.getChannel()) {

            long fileSize = fileChannel.size();
            long position = 0;

            Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
            headersFrame.headers().method("GET").path("/secure/api-all")
                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                    .scheme("https");
            headersFrame.headers().add("content-length", String.valueOf(fileSize));
            ctx.writeAndFlush(headersFrame);

            System.out.println(fileSize);

            while (position < fileSize) {
                long remaining = fileSize - position;
                int size = (int) Math.min(chunkSize, remaining);

                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, size);
                byte[] chunk = new byte[size];
                buffer.get(chunk);

                // Обрабатываем чанк
                System.out.println("Read chunk of size: " + chunk.length);
                // Здесь можно отправить `chunk` или обработать

                Http3DataFrame dataFrame = new DefaultHttp3DataFrame(Unpooled.copiedBuffer(chunk));


                position += size;
                if (position == fileSize) {
                    System.out.println("File read completed");
                }
            ctx.writeAndFlush(dataFrame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {

    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {

    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }
}
