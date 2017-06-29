package fi.fmi.avi.tac.lexer.impl;

import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.impl.METARTACSerializer;
import fi.fmi.avi.converter.impl.TAFTACSerializer;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.tac.lexer.AviMessageTACTokenizer;
import fi.fmi.avi.tac.lexer.LexemeSequence;
import fi.fmi.avi.tac.lexer.SerializingException;

public class AviMessageTACTokenizerImpl implements AviMessageTACTokenizer {
	private METARTACSerializer metarSerializer;
	private TAFTACSerializer tafSerializer;
	
	public void setMETARSerializer(METARTACSerializer serializer) {
		this.metarSerializer = serializer;
	}
	
	public void setTAFSerializer(TAFTACSerializer serializer) {
		this.tafSerializer = serializer;
	}
	
	public AviMessageTACTokenizerImpl() {
	}

	 @Override
	    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg) throws SerializingException {
	        return this.tokenizeMessage(msg, null);
	    }

	    @Override
	    public LexemeSequence tokenizeMessage(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException {
	        if (msg instanceof METAR) {
	        	return this.metarSerializer.tokenizeMessage(msg, hints);
	        } else if (msg instanceof TAF) {
	            return this.tafSerializer.tokenizeMessage(msg, hints);
	        }
	        throw new IllegalArgumentException("Do not know how to tokenize message of type " + msg.getClass().getCanonicalName());
	    }

}
