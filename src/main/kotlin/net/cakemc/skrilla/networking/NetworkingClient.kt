package net.cakemc.skrilla.networking

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import net.cakemc.skrilla.networking.codec.BossHandler
import net.cakemc.skrilla.networking.codec.compress.CompressionDecoder
import net.cakemc.skrilla.networking.codec.compress.CompressionEncoder
import net.cakemc.skrilla.networking.codec.crypt.CipherDecoder
import net.cakemc.skrilla.networking.codec.crypt.CipherEncoder
import net.cakemc.skrilla.networking.codec.packet.PacketDecoder
import net.cakemc.skrilla.networking.codec.packet.PacketEncoder
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.packet.PacketRegistry
import javax.crypto.SecretKey

class NetworkingClient(
    val secretKey: SecretKey,
    val clientHandler: ClientHandler,
    val packetRegistry: PacketRegistry
) {

    var group: EventLoopGroup? = null
        private set

    var channel: Class<out Channel?>? = null
        private set

    var bootstrap: Bootstrap? = null
        private set

    var channelFuture: ChannelFuture? = null
        private set

    /**
     * Initializes the networking client by setting up the group and determining the channel type
     * based on the available I/O model.
     */
    fun initialize() {
        val ioHandlerFactory =
            if (EPOLL) (if (KQUEUE) KQueueIoHandler.newFactory()
            else EpollIoHandler.newFactory()) else NioIoHandler.newFactory()

        this.group = MultiThreadIoEventLoopGroup(2, ioHandlerFactory)
        this.channel = if (EPOLL) (if (KQUEUE) KQueueSocketChannel::class.java
            else EpollSocketChannel::class.java) else NioSocketChannel::class.java
    }

    @Throws(InterruptedException::class)
    fun connect(host: String, port: Int) {
        try {
            val bootstrap = Bootstrap()
                .group(group)
                .channel(channel)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // Data settings

                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        // Adding handlers to the pipeline
                        pipeline.addLast(
                            CipherDecoder(secretKey),
                            CipherEncoder(secretKey),

                            CompressionDecoder(),
                            CompressionEncoder(),

                            PacketDecoder(packetRegistry),
                            PacketEncoder(),

                            BossHandler(clientHandler)
                        )
                    }
                })

            val future = bootstrap.connect(host, port).sync()
            channelFuture = future
            future.channel().closeFuture().sync()
        } finally {
            group!!.shutdownGracefully()
        }
    }

    fun disconnect() {
        channelFuture!!.channel().close().syncUninterruptibly()
        group!!.shutdownGracefully().syncUninterruptibly()
    }

    companion object {
        /**
         * Indicates whether Epoll is available for use.
         */
        val EPOLL: Boolean = Epoll.isAvailable()

        /**
         * Indicates whether KQueue is available for use.
         */
        val KQUEUE: Boolean = KQueue.isAvailable()
    }
}
