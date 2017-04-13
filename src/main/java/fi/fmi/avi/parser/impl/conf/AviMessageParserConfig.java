package fi.fmi.avi.parser.impl.conf;

import static fi.fmi.avi.parser.Lexeme.Identity.ISSUE_TIME;
import static fi.fmi.avi.parser.Lexeme.Identity.METAR_START;
import static fi.fmi.avi.parser.Lexeme.Identity.TAF_START;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.impl.AviMessageParserImpl;
import fi.fmi.avi.parser.impl.AviMessageSpecificParser;
import fi.fmi.avi.parser.impl.AviMessageTACTokenizerImpl;
import fi.fmi.avi.parser.impl.MetarParserImpl;
import fi.fmi.avi.parser.impl.lexer.AviMessageLexerImpl;
import fi.fmi.avi.parser.impl.lexer.LexingFactoryImpl;
import fi.fmi.avi.parser.impl.lexer.PrioritizedLexemeVisitor.Priority;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer;
import fi.fmi.avi.parser.impl.lexer.token.AirDewpointTemperature;
import fi.fmi.avi.parser.impl.lexer.token.Amendment;
import fi.fmi.avi.parser.impl.lexer.token.AtmosphericPressureQNH;
import fi.fmi.avi.parser.impl.lexer.token.AutoMetar;
import fi.fmi.avi.parser.impl.lexer.token.CAVOK;
import fi.fmi.avi.parser.impl.lexer.token.Cancellation;
import fi.fmi.avi.parser.impl.lexer.token.ChangeForecastTimeGroup;
import fi.fmi.avi.parser.impl.lexer.token.CloudLayer;
import fi.fmi.avi.parser.impl.lexer.token.ColorCode;
import fi.fmi.avi.parser.impl.lexer.token.Correction;
import fi.fmi.avi.parser.impl.lexer.token.ForecastChangeIndicator;
import fi.fmi.avi.parser.impl.lexer.token.ForecastMaxMinTemperature;
import fi.fmi.avi.parser.impl.lexer.token.FractionalHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.ICAOCode;
import fi.fmi.avi.parser.impl.lexer.token.IssueTime;
import fi.fmi.avi.parser.impl.lexer.token.MetarStart;
import fi.fmi.avi.parser.impl.lexer.token.MetricHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.Nil;
import fi.fmi.avi.parser.impl.lexer.token.NoSignificantWeather;
import fi.fmi.avi.parser.impl.lexer.token.Remark;
import fi.fmi.avi.parser.impl.lexer.token.RemarkStart;
import fi.fmi.avi.parser.impl.lexer.token.RunwayState;
import fi.fmi.avi.parser.impl.lexer.token.RunwayVisualRange;
import fi.fmi.avi.parser.impl.lexer.token.SeaState;
import fi.fmi.avi.parser.impl.lexer.token.SnowClosure;
import fi.fmi.avi.parser.impl.lexer.token.SurfaceWind;
import fi.fmi.avi.parser.impl.lexer.token.TAFStart;
import fi.fmi.avi.parser.impl.lexer.token.ValidTime;
import fi.fmi.avi.parser.impl.lexer.token.VariableSurfaceWind;
import fi.fmi.avi.parser.impl.lexer.token.Weather;
import fi.fmi.avi.parser.impl.lexer.token.WindShear;

/**
 * Created by rinne on 10/02/17.
 */
@Configuration
public class AviMessageParserConfig {

    @Bean
    public AviMessageLexer aviMessageLexer() {
        AviMessageLexerImpl l = new AviMessageLexerImpl();
        l.setLexingFactory(lexingFactory());
        l.addTokenLexer("METAR", metarTokenLexer());
        l.addTokenLexer("TAF", tafTokenLexer());
        return l;
    }

    @Bean
    public AviMessageParser aviMessageParser() {
        AviMessageParserImpl p = new AviMessageParserImpl();
        p.addMessageSpecificParser(Metar.class, metarParser());
        return p;
    }

    @Bean
    public AviMessageSpecificParser<Metar> metarParser() {
        return new MetarParserImpl();
    }

    @Bean
    public AviMessageTACTokenizer tacTokenizer() {
        AviMessageTACTokenizerImpl s = new AviMessageTACTokenizerImpl();
        s.setLexingFactory(lexingFactory());
        s.addReconstructor(METAR_START, new MetarStart.Reconstructor());
        s.addReconstructor(TAF_START, new TAFStart.Reconstructor());
        s.addReconstructor(ISSUE_TIME, new IssueTime.Reconstructor());
        //TODO: all the other Metar & TAF reconstructors
        return s;
    }

    @Bean
    public RecognizingAviMessageTokenLexer metarTokenLexer() {
        RecognizingAviMessageTokenLexer l = new RecognizingAviMessageTokenLexer();

        //The METAR token lexer can understand the following tokens (low priority = occurs less often):
        l.teach(new MetarStart(Priority.LOW));
        l.teach(new ICAOCode(Priority.LOW));
        l.teach(new IssueTime(Priority.LOW));
        l.teach(new CloudLayer(Priority.HIGH));
        l.teach(new Weather(Priority.NORMAL));
        l.teach(new SurfaceWind(Priority.LOW));
        l.teach(new VariableSurfaceWind(Priority.LOW));
        l.teach(new MetricHorizontalVisibility(Priority.NORMAL));
        l.teach(new FractionalHorizontalVisibility(Priority.NORMAL));
        l.teach(new ForecastChangeIndicator(Priority.LOW));
        l.teach(new ChangeForecastTimeGroup(Priority.LOW));
        l.teach(new ColorCode(Priority.LOW));
        l.teach(new CAVOK(Priority.LOW));
        l.teach(new Correction(Priority.LOW));
        l.teach(new RunwayVisualRange(Priority.HIGH));
        l.teach(new AirDewpointTemperature(Priority.LOW));
        l.teach(new AtmosphericPressureQNH(Priority.LOW));
        l.teach(new RunwayState(Priority.LOW));
        l.teach(new SnowClosure(Priority.LOW));
        l.teach(new AutoMetar(Priority.LOW));
        l.teach(new NoSignificantWeather(Priority.LOW));
        l.teach(new RemarkStart(Priority.LOW));
        l.teach(new Remark(Priority.NORMAL));
        l.teach(new WindShear(Priority.LOW));
        l.teach(new SeaState(Priority.LOW));
        return l;
    }

    @Bean
    public RecognizingAviMessageTokenLexer tafTokenLexer() {
        RecognizingAviMessageTokenLexer l = new RecognizingAviMessageTokenLexer();
        l.teach(new TAFStart(Priority.LOW));
        l.teach(new ICAOCode(Priority.LOW));
        l.teach(new ValidTime(Priority.LOW));
        l.teach(new IssueTime(Priority.LOW));
        l.teach(new CloudLayer(Priority.HIGH));
        l.teach(new Weather(Priority.NORMAL));
        l.teach(new SurfaceWind(Priority.LOW));
        l.teach(new VariableSurfaceWind(Priority.LOW));
        l.teach(new MetricHorizontalVisibility(Priority.NORMAL));
        l.teach(new FractionalHorizontalVisibility(Priority.NORMAL));
        l.teach(new ForecastChangeIndicator(Priority.LOW));
        l.teach(new ChangeForecastTimeGroup(Priority.LOW));
        l.teach(new Correction(Priority.LOW));
        l.teach(new Amendment(Priority.LOW));
        l.teach(new Nil(Priority.LOW));
        l.teach(new Cancellation(Priority.LOW));
        l.teach(new CAVOK(Priority.LOW));
        l.teach(new NoSignificantWeather(Priority.LOW));
        l.teach(new ForecastMaxMinTemperature(Priority.LOW));
        return l;
    }

    @Bean
    public LexingFactory lexingFactory() {
        return new LexingFactoryImpl();
    }

}
