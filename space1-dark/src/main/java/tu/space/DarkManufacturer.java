package tu.space;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import tu.space.components.Computer;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.Cpu;
import tu.space.components.RamModule;
import tu.space.utils.DummyFuture;
import tu.space.utils.Logger;
import tu.space.utils.UUIDGenerator;
import tu.space.utils.Util;

public class DarkManufacturer {
	public static final String USAGE = "usage: manufacturer ID";
	
	public static void main( String... args ) throws JMSException, InterruptedException, ExecutionException {
		if ( args.length != 1 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id = args[0];
		
		Logger.configure();
		final Logger log = Logger.make( DarkManufacturer.class );

		final UUIDGenerator uuids = new UUIDGenerator();
		
		final Connection conn = new ActiveMQConnectionFactory( DarkServer.BROKER_URL ).createConnection();
		final Session    sess = conn.createSession( true, Session.SESSION_TRANSACTED );
	
		final Queue cpuQ = sess.createQueue( "cpu" );
		final Queue gpuQ = sess.createQueue( "gpu" );
		final Queue mbdQ = sess.createQueue( "mainboard" );
		final Queue ramQ = sess.createQueue( "ram" );
		final Topic cpuT = sess.createTopic( "cpu" );
		final Topic gpuT = sess.createTopic( "gpu" );
		final Topic mbdT = sess.createTopic( "mainboard" );
		final Topic ramT = sess.createTopic( "ram" );

		final Queue pcQ = sess.createQueue( "computer" );
		final Topic pcT = sess.createTopic( "computer" );
		
		final MessageProducer pcQOut = sess.createProducer( pcQ );
		final MessageProducer pcTOut = sess.createProducer( pcT );

		final MessageConsumer cpuIn = sess.createConsumer( cpuQ );
		final MessageConsumer gpuIn = sess.createConsumer( gpuQ );
		final MessageConsumer mbdIn = sess.createConsumer( mbdQ );
		final MessageConsumer ramIn = sess.createConsumer( ramQ );
		
		final MessageProducer ramQOut = sess.createProducer( ramQ );
		final MessageProducer cpuTOut = sess.createProducer( cpuT );
		final MessageProducer gpuTOut = sess.createProducer( gpuT );
		final MessageProducer mbdTOut = sess.createProducer( mbdT );
		final MessageProducer ramTOut = sess.createProducer( ramT );
		
		final ExecutorService ex = Executors.newCachedThreadPool();
		
		conn.start();
		
		new Thread() {
			public void run() {
				System.out.println("PRESS ENTER TO QUIT");
				Util.waitForNewline();

				JMS.close( conn );
				ex.shutdown();
			}
		}.start();
		
		try {
			while ( true ) {
				Future<Cpu>       cpuF  = getComponent( ex, cpuIn, log, id, "CPU" );
				Future<Gpu>       gpuF  = getComponent( ex, gpuIn, log, id, "GPU" );
				Future<Mainboard> mbdF  = getComponent( ex, mbdIn, log, id, "Mainboard" );
				Future<RamModule> ram1F = getComponent( ex, ramIn, log, id, "RAM" );
				Future<RamModule> ram2F = getComponent( ex, ramIn, log, id, "RAM" );
				Future<RamModule> ram3F = getComponent( ex, ramIn, log, id, "RAM" );
				Future<RamModule> ram4F = getComponent( ex, ramIn, log, id, "RAM" );
				
				UUID      uuid  = uuids.generate();
				Cpu       cpu   = cpuF.get();
				Mainboard mbd   = mbdF.get();
				RamModule ram1  = ram1F.get();
				RamModule ram2  = getOrCancel( ram2F );
				RamModule ram3  = getOrCancel( ram3F );
				RamModule ram4  = getOrCancel( ram4F );
				Gpu       gpu   = getOrCancel( gpuF );
				
				List<RamModule> ram = new ArrayList<RamModule>();
				if ( ram1 != null ) ram.add( ram1 );
				if ( ram2 != null ) ram.add( ram2 );
				if ( ram3 != null ) ram.add( ram3 );
				if ( ram4 != null ) ram.add( ram4 );
				
				if ( ram.size() == 3 ) {
					RamModule back = ram.remove( 0 );
					ramQOut.send( JMS.toMessage( sess, back ) );
				}
				log.info("%s is using %d pieces of RAM", id, ram.size() );
					
				Computer c = new Computer( uuid, id, cpu, gpu, mbd, ram );				
				
				cpuTOut.send( JMS.toRemovedMessage( sess, cpu ) );
				gpuTOut.send( JMS.toRemovedMessage( sess, gpu ) );
				mbdTOut.send( JMS.toRemovedMessage( sess, mbd ) );
				for ( RamModule r : ram ) ramTOut.send( JMS.toRemovedMessage( sess, r ) );
				
				pcQOut.send( JMS.toMessage( sess, c ) );
				pcTOut.send( JMS.toCreatedMessage( sess, c ) );

				sess.commit();
				
				log.info("%s made a PC", id );
			}
		} catch ( JMSException e ) {
//			sess.rollback();
		}
		
//		final Middleware m = new JMSMiddlewareFactory().make();
//		new Manufacturer( id, m ).run();
	}
	
	private static <E extends Serializable> Future<E> getComponent( 
		ExecutorService ex, final MessageConsumer m, final Logger log, final String id, final String type ) {
		
		if ( ex.isTerminated() ) return new DummyFuture<E>();
		
		return ex.submit( new Callable<E>() {
			@Override
			public E call() throws Exception {
				Message in = m.receive();
				if ( in == null ) return null;
				
				ObjectMessage msg = (ObjectMessage) in;

				@SuppressWarnings("unchecked")
				E e = (E) msg.getObject();
				log.info("%s got %s", id, type);
				
				return e;
			}
		});
	}
	
	private static <E> E getOrCancel( Future<E> f ) {
		try {
			if ( f.isDone() ) {
				return f.get();
			} else {
				f.cancel( true );
				return null;
			}
		} catch ( InterruptedException e ) {
			return null;
		} catch ( ExecutionException e ) {
			return null;
		}
	}
}
