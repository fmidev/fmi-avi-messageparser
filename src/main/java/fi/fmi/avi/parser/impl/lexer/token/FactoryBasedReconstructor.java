package fi.fmi.avi.parser.impl.lexer.token;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.impl.lexer.TACTokenReconstructor;

/**
 * Created by rinne on 01/03/17.
 */
public abstract class FactoryBasedReconstructor<T extends AviationWeatherMessage> implements TACTokenReconstructor {
    private LexingFactory factory;

    public void setLexingFactory(final LexingFactory factory) {
        this.factory = factory;
    }

    public LexingFactory getLexingFactory() {
        return this.factory;
    }

}
