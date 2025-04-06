package net.cakemc.skrilla.networking

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
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

class NetworkingServer(
    val secretKey: SecretKey,
    val clientHandler: ClientHandler,
    val packetRegistry: PacketRegistry = PacketRegistry()
) {

    /**
     * Returns the boss event loop group for the server.
     *
     * @return the [EventLoopGroup] used for accepting connections
     */
    // JsonContainer
    var bossGroup: EventLoopGroup? = null
        private set

    /**
     * Returns the worker event loop group for the server.
     *
     * @return the [EventLoopGroup] used for processing connections
     */
    var workerGroup: EventLoopGroup? = null
        private set

    /**
     * Returns the class of the server channel being used.
     *
     * @return the [Class] of the server channel
     */
    var channel: Class<out ServerChannel?>? = null
        private set

    /**
     * Returns the [ServerBootstrap] instance used to configure the server.
     *
     * @return the [ServerBootstrap] instance
     */
    // Server
    var serverBootstrap: ServerBootstrap? = null
        private set

    /**
     * Returns the [ChannelFuture] representing the future of the server's channel.
     *
     * @return the [ChannelFuture] for the server channel
     */
    var channelFuture: ChannelFuture? = null
        private set

    /**
     * Initializes the network server by setting up the boss and worker event loop groups
     * and determining the channel type based on the available I/O model.
     */
    fun initialize() {
        val ioHandlerFactory =
            if (EPOLL) (if (KQUEUE) KQueueIoHandler.newFactory()
            else EpollIoHandler.newFactory()) else NioIoHandler.newFactory()

        this.bossGroup = MultiThreadIoEventLoopGroup(2, ioHandlerFactory)
        this.workerGroup = MultiThreadIoEventLoopGroup(2, ioHandlerFactory)

        this.channel =
            if (EPOLL) (if (KQUEUE) KQueueServerSocketChannel::class.java
            else EpollServerSocketChannel::class.java) else NioServerSocketChannel::class.java
    }

    @Throws(InterruptedException::class)
    fun start(host: String, port: Int) {
        try {
            val bootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channel)

                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // Data settings

                .childOption(ChannelOption.WRITE_SPIN_COUNT, 1)

                .option(ChannelOption.SO_REUSEADDR, true)

                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

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
            val future = bootstrap.bind(host, port).sync()
            future.channel().closeFuture().sync()
        } finally {
            bossGroup!!.shutdownGracefully()
            workerGroup!!.shutdownGracefully()
        }
    }

    fun shutdown() {
        channelFuture!!.channel().close().syncUninterruptibly()
        bossGroup!!.shutdownGracefully().syncUninterruptibly()
        workerGroup!!.shutdownGracefully().syncUninterruptibly()
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
