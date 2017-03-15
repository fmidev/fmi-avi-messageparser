package fi.fmi.avi.parser.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.impl.MetarImpl;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.impl.TAFImpl;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexemeSequence;
import fi.fmi.avi.parser.ParsingException;
import fi.fmi.avi.parser.ParsingHints;

/**
 * Created by rinne on 13/12/16.
 */
public class AviMessageParserImpl implements AviMessageParser {
    private static final Logger LOG = LoggerFactory.getLogger(AviMessageParserImpl.class);

    @Override
    public <T extends AviationWeatherMessage> T parseMessage(final LexemeSequence lexed, final Class<T> type) throws ParsingException {
        return parseMessage(lexed, type, null);
    }

    @Override
    public <T extends AviationWeatherMessage> T parseMessage(final LexemeSequence lexed, final Class<T> type, final ParsingHints hints)
            throws ParsingException {
        LOG.info(lexed.getFirstLexeme().getStatus().toString());
        if (Lexeme.Status.OK == lexed.getFirstLexeme().getStatus()) {
            if (Metar.class.isAssignableFrom(type)) {
                if (Lexeme.Identity.METAR_START == lexed.getFirstLexeme().getIdentity()) {
                    checkMetarLexingResult(lexed);
                    return (T) parseMetar(lexed, hints);

                } else {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "The first lexeme is not METAR start token");
                }
            } else if (TAF.class.isAssignableFrom(type)) {
                if (Lexeme.Identity.TAF_START == lexed.getFirstLexeme().getIdentity()) {
                    checkTAFLexingResult(lexed);
                    return (T) parseTAF(lexed, hints);
                } else {
                    throw new ParsingException(ParsingException.Type.SYNTAX_ERROR, "The first lexeme is not TAF start token");
                }
            }
        }
        throw new IllegalArgumentException("Unable to parse messsage of type " + type.getCanonicalName());
    }

    private static void checkMetarLexingResult(final LexemeSequence result) throws ParsingException {
        //TODO
    }

    private static void checkTAFLexingResult(final LexemeSequence result) throws ParsingException {
        //TODO
    }

    private static Metar parseMetar(final LexemeSequence lexed, final ParsingHints hints) {
        Metar retval = new MetarImpl();
        //TODO: parsing
        return retval;
    }

    private static TAF parseTAF(final LexemeSequence lexed, final ParsingHints hints) {
        TAFImpl retval = new TAFImpl();
        updateBaseForecast(retval, lexed, hints);
        updateChangeForecasts(retval, lexed, hints);
        return retval;
    }

    private static void updateBaseForecast(final TAFImpl taf, final LexemeSequence lexed, final ParsingHints hints) {
        //TODO
    }

    private static void updateChangeForecasts(final TAFImpl taf, final LexemeSequence lexed, final ParsingHints hints) {
        //TODO
    }


}
