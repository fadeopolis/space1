package tu.space;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

import tu.space.components.Computer;
import tu.space.components.Cpu;
import tu.space.components.Gpu;
import tu.space.components.Mainboard;
import tu.space.components.RamModule;
import tu.space.utils.DummyFuture;
import tu.space.utils.Logger;
import tu.space.utils.UUIDGenerator;
import tu.space.utils.Util;

public class DarkManufacturer {
	public static final String USAGE = "usage: manufacturer ID";
	
	public static void main( String... args ) throws JMSException {
		if ( args.length != 1 ) {
			System.err.println( USAGE );
			System.exit( 1 );
		}
		final String id = args[0];
		
		Logger.configure();
		final Logger log = Logger.make( DarkManufacturer.class );

		final UUIDGenerator uuids = new UUIDGenerator();
		
		final Connection conn = JMS.openConnection();
		final Session    sess = JMS.createSession( conn );
	
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
		
		conn.start();
		final ExecutorService ex = Executors.newCachedThreadPool();
		
		new Thread() {
			public void run() {
				System.out.println("PRESS ENTER TO QUIT");
				Util.waitForNewline();

				ex.shutdownNow();
				JMS.close( conn );
			}
		}.start();
		
		try {
			while ( true ) {
				Future<Cpu>             cpuF  = getComponent( ex, cpuIn, log, id, "CPU" );
				Future<Gpu>             gpuF  = getComponent( ex, gpuIn, log, id, "GPU" );
				Future<Mainboard>       mbdF  = getComponent( ex, mbdIn, log, id, "Mainboard" );
				Future<List<RamModule>> ramF  = getRam( ex, ramIn, log, id );
				
				UUID            uuid  = uuids.generate();
				Cpu             cpu   = cpuF.get();
				Gpu             gpu   = getOrCancel( gpuF );
				Mainboard       mbd   = mbdF.get();
				List<RamModule> ram   = ramF.get();

				// return unused pieces
				if ( ram.size() == 3 ) {
					RamModule back = ram.remove( 0 );
					ramQOut.send( JMS.toMessage( sess, back ) );
				}
				log.info("%s is using %d pieces of RAM", id, ram.size() );
					
				Computer c = new Computer( uuid, id, cpu, gpu, mbd, ram );				
				
				// simulate work
				Util.sleep( 3000 );
				
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
		} catch ( InterruptedException e ) {
		} catch ( ExecutionException e ) {
		} finally {
			log.info("%s finished", id );
			if ( ex != null ) ex.shutdownNow();
			JMS.close( conn );
		}
	}
	
	private static <E extends Serializable> Future<E> getComponent( 
		ExecutorService ex, final MessageConsumer queue, 
		final Logger log, final String id, final String type ) {
		
		if ( ex.isTerminated() ) return new DummyFuture<E>();
		
		return ex.submit( new Callable<E>() {
			@Override
			public E call() throws Exception {
				Message in = queue.receive();
				if ( in == null ) return null;
				
				ObjectMessage msg = (ObjectMessage) in;

				@SuppressWarnings("unchecked")
				E e = (E) msg.getObject();
				log.info("%s got a %s", id, type);
				
				return e;
			}
		});
	}
	private static Future<List<RamModule>> getRam( 
			ExecutorService ex, final MessageConsumer queue, 
			final Logger log, final String id ) {
		if ( ex.isTerminated() ) return DummyFuture.make( Collections.<RamModule>emptyList() );
			
		return ex.submit( new Callable<List<RamModule>>() {
			@Override
			public List<RamModule> call() throws Exception {
				List<RamModule> ram = new ArrayList<RamModule>();
				
				Message in;
				
				in = queue.receive();
				if ( in == null ) return ram;
				
				ram.add( (RamModule) ((ObjectMessage) in).getObject() );
				log.info("%s got a RAM module", id);
				
				in = queue.receiveNoWait();
				if ( in != null ) {
					ram.add( (RamModule) ((ObjectMessage) in).getObject() );
					log.info("%s got a RAM module", id);
				}				
					
				return ram;
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
