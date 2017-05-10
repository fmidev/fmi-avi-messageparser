package fi.fmi.avi.parser.impl.lexer;

import fi.fmi.avi.parser.LexingFactory;

/**
 * Created by rinne on 01/03/17.
 */
public abstract class FactoryBasedReconstructor implements TACTokenReconstructor {
    private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

}
