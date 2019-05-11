package com.github.upcraftlp.votifier.net;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.RawVote;
import com.github.upcraftlp.votifier.api.RawVoteEvent;
import com.github.upcraftlp.votifier.util.RSAUtil;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class NetworkListenerThread extends Thread {

    private final String host;
    private final int port;
    private boolean isRunning = true;

    public NetworkListenerThread(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(this.host, this.port));
            ForgeVotifier.getLogger().info("votifier {} running on {}:{}", ForgeVotifier.VERSION, this.host, this.port);
            while(this.isRunning) {
                try(Socket socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000); //workaround for slow connections
                    try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); InputStream inputStream = socket.getInputStream()) {
                        writer.write("FORGE VOTIFIER " + ForgeVotifier.VERSION + " on " + this.host + ":" + this.port);
                        writer.newLine();
                        writer.flush();
                        byte[] bytes = new byte[256];
                        int readable = inputStream.read(bytes, 0, bytes.length);
                        if (readable == 256) {
                            String[] lines = new String(RSAUtil.decrypt(bytes, RSAUtil.getKeyPair().getPrivate()), StandardCharsets.UTF_8).split("\n");
                            if(lines.length < 4) {
                                error(lines);
                            }
                            else {
                                String opcode = lines[0].trim();
                                if("VOTE".equals(opcode)) {
                                    String service = lines[1].trim();
                                    String username = lines[2].trim();
                                    String address = lines[3].trim();
                                    String timestamp = lines.length >= 5 ? lines[4].trim() : Long.toString(System.nanoTime() / 1_000_000L);
                                    FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> { //ensure we are not handling the event on the network thread
                                        ForgeVotifier.getLogger().info("[{}] received vote from {} (service: {})", timestamp, username, service);
                                        MinecraftForge.EVENT_BUS.post(new RawVoteEvent(new RawVote(service, username, address, timestamp)));
                                    });
                                }
                                else {
                                    error(lines);
                                }
                            }
                        }
                        else {
                            ForgeVotifier.getLogger().error("Unknown Packet Ignoring: "+readable);
                        }
                    }
                    catch (IOException e) {
                        ForgeVotifier.getLogger().error("Error handling socket connection!", e);
                    }
                }
                catch (IOException e) {
                    ForgeVotifier.getLogger().error("Error handling socket connection!", e);
                }
            }
        }
        catch (IOException e) {
            this.isRunning = false;
            ForgeVotifier.getLogger().error("Votifier network error! Host: " + this.host + ", Port: " + this.port, e);
        }
        ForgeVotifier.getLogger().info("votifier thread is set to shut down!\tVotifier {}", ForgeVotifier.VERSION);
    }

    private static void error(String[] input) {
        StringBuilder builder = new StringBuilder();
        for(String line : input) {
            builder.append(line).append("\n");
        }
        ForgeVotifier.getLogger().error("Votifier: incorrect vote received:\n-----------------------------------------------------\n{}-----------------------------------------------------", builder.toString());
    }

    public void shutdown() {
        this.isRunning = false;
        this.interrupt();
        ForgeVotifier.getLogger().info("votifier thread stopped!");
    }
}
