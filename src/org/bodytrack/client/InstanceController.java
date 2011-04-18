package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that makes instance control easier for other classes.
 *
 * <p>This class is specifically designed for the case in which
 * a class keeps a series of instances, one for each argument to
 * some factory method.  For this pattern to work, though, we
 * really need some kind of immutable object as our cached type
 * T.  If this is not the case, caching can produce an object that
 * has been changed, which breaks the instance control.</p>
 *
 * @param <P>
 * 		the type of parameter that is used to generate new instances
 * 		of whichever class we are controlling instances of
 * @param <T>
 * 		the type of the object we generate on each instance
 */
public final class InstanceController<P, T> {
	private final InstanceProducer<P, T> producer;
	private final Map<P, T> instances;

	/**
	 * Creates a new <tt>InstanceController</tt> that uses
	 * the specified producer.
	 *
	 * @param producer
	 * 		some object that can produce new objects of type T
	 * 		given parameters of type P
	 * @throws NullPointerException
	 * 		if producer is <tt>null</tt>
	 */
	public InstanceController(InstanceProducer<P, T> producer) {
		if (producer == null)
			throw new NullPointerException(
				"Can't produce instances from null");

		this.producer = producer;
		this.instances = new HashMap<P, T>();
	}

	/**
	 * Returns a new instance if necessary, or a cached instance if
	 * possible.
	 *
	 * @param param
	 * 		the parameter for construction of a new object (using
	 * 		the producer passed to the constructor for this
	 * 		<tt>InstanceProducer</tt>)
	 * @return
	 * 		either a cached instance of T, or a new instance of	T
	 * 		generated through the {@link InstanceProducer#newInstance(Object)}
	 * 		method.  If an object is generated, it is cached for next time
	 */
	public T newInstance(P param) {
		if (instances.containsKey(param))
			return instances.get(param);

		T result = producer.newInstance(param);
		instances.put(param, result);
		return result;
	}
	
	/**
	 * An interface describing a class that produces new instances of
	 * another class.
	 *
	 * @param <P>
	 * 		the type of parameter used to determine which object to
	 * 		produce
	 * @param <T>
	 * 		the type of object that is produced
	 */
	public interface InstanceProducer<P, T> {
		/**
		 * Creates a new instance of a T.
		 *
		 * @param param
		 * 		some parameter for construction
		 * @return
		 * 		some non-<tt>null</tt> object of type T
		 */
		public T newInstance(P param);
	}
}
