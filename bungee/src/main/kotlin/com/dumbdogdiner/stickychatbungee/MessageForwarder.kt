package com.dumbdogdiner.stickychatbungee

import com.dumbdogdiner.stickychatcommon.Constants
import com.dumbdogdiner.stickychatcommon.MessageType
import com.dumbdogdiner.stickychatcommon.MessageHandler
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

/**
 * Forwards messages from a single node to other server nodes.
 */
object MessageForwarder : Base, Listener, MessageHandler {

    private const val CHANNEL_NAME = Constants.CHANNEL_NAME

    /**
     * Handle a plugin message received from a server instance.
     */
    @EventHandler
    fun onPluginMessageReceived(ev: PluginMessageEvent) {
        if (ev.tag != CHANNEL_NAME) {
            return
        }

        logger.info("Received message from node - decoding")

        // read type of packet
        val input = ByteStreams.newDataInput(ev.data)
        val type: MessageType = MessageType.values()[input.readShort().toInt()]

        // extract data
        val len = input.readShort()
        val msgbytes = ByteArray(len.toInt())
        input.readFully(msgbytes)
        val msgin = DataInputStream(ByteArrayInputStream(msgbytes))

        when (type) {
            MessageType.MESSAGE -> handleMessage(
                msgin
            )
            MessageType.PRIVATE_MESSAGE -> handlePrivateMessage(msgin)
            MessageType.PRIVATE_MESSAGE_ACK -> handlePrivateMessageAck(msgin)
            MessageType.MAIL -> handleMailReceive(msgin)
        }
    }

    override fun handleMessage(data: DataInputStream) {
        logger.info("Got message packet - broadcasting to nodes")

        // this is dumb
        val uuid = data.readUTF()
        val name = data.readUTF()
        val content = data.readUTF()

        val msg = ByteStreams.newDataOutput()
        msg.writeUTF(uuid)
        msg.writeUTF(name)
        msg.writeUTF(content)

        val out = ByteStreams.newDataOutput()
        out.writeShort(MessageType.MESSAGE.ordinal)
        out.writeShort(msg.toByteArray().size)
        out.write(msg.toByteArray())

        sendPluginMessage(out)
    }

    override fun handlePrivateMessage(data: DataInputStream) {
        logger.info("Got private message packet - attempting to forward to targeted player")

        val uuid = data.readUTF()
        val name = data.readUTF()
        val content = data.readUTF()
        val nonce = data.readInt()

        val msg = ByteStreams.newDataOutput()
        msg.writeUTF(uuid)
        msg.writeUTF(name)
        msg.writeUTF(content)
        msg.writeShort(nonce)

        val out = ByteStreams.newDataOutput()
        out.writeShort(MessageType.PRIVATE_MESSAGE.ordinal)
        out.writeShort(msg.toByteArray().size)
        out.write(msg.toByteArray())

        sendTargetedPluginMessage(uuid, out)
    }

    override fun handlePrivateMessageAck(data: DataInputStream) {
        logger.info("Got private messace ACK packet - attempting to forward to targeted player")

        val uuid = data.readUTF()
        val nonce = data.readInt()

        val msg = ByteStreams.newDataOutput()
        msg.writeUTF(uuid)
        msg.writeInt(nonce)

        val out = ByteStreams.newDataOutput()
        out.writeShort(MessageType.PRIVATE_MESSAGE_ACK.ordinal)
        out.writeShort(msg.toByteArray().size)
        out.write(msg.toByteArray())

        sendTargetedPluginMessage(uuid, out)
    }

    override fun handleMailReceive(data: DataInputStream) {
        logger.info("Got mail received packet - broadcasting to nodes")

        val uuid = data.readUTF()
        val name = data.readUTF()
        val to = data.readUTF()
        val content = data.readUTF()

        val msg = ByteStreams.newDataOutput()
        msg.writeUTF(uuid)
        msg.writeUTF(name)
        msg.writeUTF(to)
        msg.writeUTF(content)

        val out = ByteStreams.newDataOutput()
        out.writeShort(MessageType.MAIL.ordinal)
        out.writeShort(msg.toByteArray().size)
        out.write(msg.toByteArray())

        sendPluginMessage(out)
    }

    /**
     * Sends a global plugin message to all servers - not guaranteed to be received on
     * all servers.
     */
    override fun sendPluginMessage(data: ByteArrayDataOutput) {
        proxy.servers.values.forEach {
            it.players.firstOrNull()?.sendData(CHANNEL_NAME, data.toByteArray())
        }
    }

    /**
     * Send a plugin message to the player with the specified id - not guaranteed to be
     * received.
     */
    override fun sendTargetedPluginMessage(uuid: String, data: ByteArrayDataOutput) {
        for (server in proxy.servers.values) {
            for (player in server.players) {
                if (player.uniqueId.toString() == uuid) {
                    player.sendData(CHANNEL_NAME, data.toByteArray())
                    return
                }
            }
        }
        logger.warning("Failed to find proxied player with uuid '$uuid'")
    }
}
