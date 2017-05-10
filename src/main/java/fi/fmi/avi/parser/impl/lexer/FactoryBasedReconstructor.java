package fi.fmi.avi.parser.impl.lexer;

import fi.fmi.avi.parser.LexingFactory;

/**
 * Created by rinne on 01/03/17.
 */
public abstract class FactoryBasedReconstructor implements TACTokenReconstructor {
	
	protected static <T> T getAs(Object[] specifiers, Class<T> clz) {
		return getAs(specifiers, 0, clz);
	}
	
	@SuppressWarnings("unchecked")
	protected static <T> T getAs(Object[] specifiers, final int index, Class<T> clz) {
		T retval = null;
		if (specifiers != null && specifiers.length > index && clz.isAssignableFrom(specifiers[index].getClass())) {
			retval = (T) specifiers[index];
		}
		return retval;
	}
	
	private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

}
