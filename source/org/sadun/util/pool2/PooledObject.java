package org.sadun.util.pool2;


/**
 * A pooled object is a stub for its original object, which is
 * accessible via the same interface.
 * <p>
 * The original underlying object is not necessarily in core; if a
 * {@link BasePassivationManager passivation manager class} is
 * associated with the {@link ObjectPool ObjectPool} containing the
 * object, and the object is passivable according to that manager,
 * the original object may be passivated on some secondary storage.
 * 
 * @author Cristiano Sadun
 * 
 */
public interface PooledObject {
	
	/**
	 * Activate the object, if necessary, and returns the original
	 * object. The object stays in core until is released by
	 * {@link #_releaseOriginal() releaseOriginal()}.
	 */
	public Object _getOriginal() throws ActivationException;
	
	/**
	 * Release the original, marking it as passivable.
	 */
	public void _releaseOriginal();
	
	/**
	 * Passivate the object - storing it on secondary storage and
	 * freeing core memory.
	 */
	public void _passivate() throws PassivationException;
	
	/**
	 * Activate the object - retrieving it from secondary memory.
	 * The object is still passivable.
	 */
	public void _activate() throws ActivationException;
	
	/**
	 * Return the invariant passivable.state 
	 * @return boolean
	 */
	public boolean _isPassivable();
	
	/**
	 * Return the current passivable.state
	 * @return boolean
	 */
	public boolean _isPassivableNow();
	
	/**
	 * Return the passivate.state
	 * @return boolean
	 */
	public boolean _isPassivated();

}
