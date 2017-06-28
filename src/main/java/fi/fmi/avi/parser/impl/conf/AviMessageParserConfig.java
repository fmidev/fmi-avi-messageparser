package fi.fmi.avi.parser.impl.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.fmi.avi.data.metar.METAR;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.parser.AviMessageLexer;
import fi.fmi.avi.parser.AviMessageParser;
import fi.fmi.avi.parser.AviMessageSerializer;
import fi.fmi.avi.parser.AviMessageTACTokenizer;
import fi.fmi.avi.parser.ConversionSpecification;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.LexingFactory;
import fi.fmi.avi.parser.impl.AviMessageParserImpl;
import fi.fmi.avi.parser.impl.AviMessageSerializerImpl;
import fi.fmi.avi.parser.impl.AviMessageSpecificParser;
import fi.fmi.avi.parser.impl.METARTACParser;
import fi.fmi.avi.parser.impl.METARTACSerializer;
import fi.fmi.avi.parser.impl.TACParser;
import fi.fmi.avi.parser.impl.TAFTACParser;
import fi.fmi.avi.parser.impl.TAFTACSerializer;
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
import fi.fmi.avi.parser.impl.lexer.token.EndToken;
import fi.fmi.avi.parser.impl.lexer.token.ForecastChangeIndicator;
import fi.fmi.avi.parser.impl.lexer.token.ForecastMaxMinTemperature;
import fi.fmi.avi.parser.impl.lexer.token.FractionalHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.ICAOCode;
import fi.fmi.avi.parser.impl.lexer.token.IssueTime;
import fi.fmi.avi.parser.impl.lexer.token.MetarStart;
import fi.fmi.avi.parser.impl.lexer.token.MetricHorizontalVisibility;
import fi.fmi.avi.parser.impl.lexer.token.Nil;
import fi.fmi.avi.parser.impl.lexer.token.NoSignificantCloud;
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
        p.addMessageSpecificParser(ConversionSpecification.TAC_TO_METAR_POJO, metarTACParser());
        p.addMessageSpecificParser(ConversionSpecification.TAC_TO_TAF_POJO, tafTACParser());
        return p;
    }

    AviMessageSpecificParser<String, METAR> metarTACParser() {
        TACParser<String, METAR> p = new METARTACParser();
        p.setTACLexer(aviMessageLexer());
        return p;
    }

    AviMessageSpecificParser<String, TAF> tafTACParser() {
        TACParser<String, TAF> p = new TAFTACParser();
        p.setTACLexer(aviMessageLexer());
        return p;
    }

    @Bean
    public AviMessageSerializer aviMessageSerializer() {
        AviMessageSerializerImpl s = new AviMessageSerializerImpl();
        s.addMessageSpecificSerializer(ConversionSpecification.METAR_POJO_TO_TAC, metarTACSerializer());
        s.addMessageSpecificSerializer(ConversionSpecification.TAF_POJO_TO_TAC, tafTACSerializer());
        return s;
    }

    @Bean
    public AviMessageTACTokenizer tacTokenizer() {
        return (AviMessageTACTokenizer) aviMessageSerializer();
    }

    METARTACSerializer metarTACSerializer() {
        METARTACSerializer s = new METARTACSerializer();
        s.setLexingFactory(lexingFactory());
        s.addReconstructor(Lexeme.Identity.METAR_START, new MetarStart.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CORRECTION, new Correction.Reconstructor());
        s.addReconstructor(Lexeme.Identity.AERODROME_DESIGNATOR, new ICAOCode.Reconstructor());
        s.addReconstructor(Lexeme.Identity.ISSUE_TIME, new IssueTime.Reconstructor());
        s.addReconstructor(Lexeme.Identity.AUTOMATED, new AutoMetar.Reconstructor());
        s.addReconstructor(Lexeme.Identity.SURFACE_WIND, new SurfaceWind.Reconstructor());
        s.addReconstructor(Lexeme.Identity.VARIABLE_WIND_DIRECTION, new VariableSurfaceWind.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CAVOK, new CAVOK.Reconstructor());
        s.addReconstructor(Lexeme.Identity.HORIZONTAL_VISIBILITY, new MetricHorizontalVisibility.Reconstructor());
        s.addReconstructor(Lexeme.Identity.WEATHER, new Weather.Reconstructor(false));
        s.addReconstructor(Lexeme.Identity.NO_SIGNIFICANT_WEATHER, new NoSignificantWeather.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CLOUD, new CloudLayer.Reconstructor());
        s.addReconstructor(Lexeme.Identity.NO_SIGNIFICANT_CLOUD, new NoSignificantCloud.Reconstructor());
        s.addReconstructor(Lexeme.Identity.FORECAST_CHANGE_INDICATOR, new ForecastChangeIndicator.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP, new ChangeForecastTimeGroup.Reconstructor());
        s.addReconstructor(Lexeme.Identity.RUNWAY_VISUAL_RANGE, new RunwayVisualRange.Reconstructor());
        s.addReconstructor(Lexeme.Identity.AIR_DEWPOINT_TEMPERATURE, new AirDewpointTemperature.Reconstructor());
        s.addReconstructor(Lexeme.Identity.AIR_PRESSURE_QNH, new AtmosphericPressureQNH.Reconstructor());
        s.addReconstructor(Lexeme.Identity.RECENT_WEATHER, new Weather.Reconstructor(true));
        s.addReconstructor(Lexeme.Identity.WIND_SHEAR, new WindShear.Reconstructor());
        s.addReconstructor(Lexeme.Identity.SEA_STATE, new SeaState.Reconstructor());
        s.addReconstructor(Lexeme.Identity.RUNWAY_STATE, new RunwayState.Reconstructor());
        s.addReconstructor(Lexeme.Identity.COLOR_CODE, new ColorCode.Reconstructor());
        s.addReconstructor(Lexeme.Identity.REMARKS_START, new RemarkStart.Reconstructor());
        s.addReconstructor(Lexeme.Identity.END_TOKEN, new EndToken.Reconstructor());
        return s;
    }

    TAFTACSerializer tafTACSerializer() {
        TAFTACSerializer s = new TAFTACSerializer();
        s.setLexingFactory(lexingFactory());
        s.addReconstructor(Lexeme.Identity.TAF_START, new TAFStart.Reconstructor());
        s.addReconstructor(Lexeme.Identity.AMENDMENT, new Amendment.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CORRECTION, new Correction.Reconstructor());
        s.addReconstructor(Lexeme.Identity.AERODROME_DESIGNATOR, new ICAOCode.Reconstructor());
        s.addReconstructor(Lexeme.Identity.ISSUE_TIME, new IssueTime.Reconstructor());
        s.addReconstructor(Lexeme.Identity.NIL, new Nil.Reconstructor());
        s.addReconstructor(Lexeme.Identity.VALID_TIME, new ValidTime.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CANCELLATION, new Cancellation.Reconstructor());
        s.addReconstructor(Lexeme.Identity.SURFACE_WIND, new SurfaceWind.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CAVOK, new CAVOK.Reconstructor());
        s.addReconstructor(Lexeme.Identity.HORIZONTAL_VISIBILITY, new MetricHorizontalVisibility.Reconstructor());
        s.addReconstructor(Lexeme.Identity.WEATHER, new Weather.Reconstructor(false));
        s.addReconstructor(Lexeme.Identity.NO_SIGNIFICANT_WEATHER, new NoSignificantWeather.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CLOUD, new CloudLayer.Reconstructor());
        s.addReconstructor(Lexeme.Identity.NO_SIGNIFICANT_CLOUD, new NoSignificantCloud.Reconstructor());
        s.addReconstructor(Lexeme.Identity.MAX_TEMPERATURE, new ForecastMaxMinTemperature.Reconstructor());
        // No need to register MIN_TEMPERATURE as ForecastMaxMinTemperature.Reconstructor will do both if both set
        s.addReconstructor(Lexeme.Identity.FORECAST_CHANGE_INDICATOR, new ForecastChangeIndicator.Reconstructor());
        s.addReconstructor(Lexeme.Identity.CHANGE_FORECAST_TIME_GROUP, new ChangeForecastTimeGroup.Reconstructor());
        s.addReconstructor(Lexeme.Identity.REMARKS_START, new RemarkStart.Reconstructor());
        s.addReconstructor(Lexeme.Identity.END_TOKEN,new EndToken.Reconstructor());
        return s;
    }

    RecognizingAviMessageTokenLexer metarTokenLexer() {
        RecognizingAviMessageTokenLexer l = new RecognizingAviMessageTokenLexer();

        //The METAR token lexer can understand the following tokens (low priority = occurs less often):
        l.teach(new MetarStart(Priority.LOW));
        l.teach(new ICAOCode(Priority.LOW));
        l.teach(new IssueTime(Priority.LOW));
        l.teach(new CloudLayer(Priority.HIGH));
        l.teach(new NoSignificantCloud(Priority.LOW));
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
        l.teach(new EndToken(Priority.LOW));
        return l;
    }

    RecognizingAviMessageTokenLexer tafTokenLexer() {
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
        l.teach(new RemarkStart(Priority.LOW));
        l.teach(new Remark(Priority.NORMAL));
        l.teach(new EndToken(Priority.LOW));
        return l;
    }

    @Bean
    public LexingFactory lexingFactory() {
        return new LexingFactoryImpl();
    }

}
