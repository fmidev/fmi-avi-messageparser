package fi.fmi.avi.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.CloudForecast;
import fi.fmi.avi.data.CloudLayer;
import fi.fmi.avi.data.NumericMeasure;
import fi.fmi.avi.data.metar.HorizontalVisibility;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.metar.ObservedClouds;
import fi.fmi.avi.data.metar.ObservedSurfaceWind;
import fi.fmi.avi.data.metar.RunwayState;
import fi.fmi.avi.data.metar.RunwayVisualRange;
import fi.fmi.avi.data.metar.SeaState;
import fi.fmi.avi.data.metar.TrendForecast;
import fi.fmi.avi.data.metar.TrendForecastSurfaceWind;
import fi.fmi.avi.data.metar.TrendTimeGroups;
import fi.fmi.avi.data.metar.WindShear;
import fi.fmi.avi.data.taf.TAF;
import fi.fmi.avi.data.taf.TAFAirTemperatureForecast;
import fi.fmi.avi.data.taf.TAFBaseForecast;
import fi.fmi.avi.data.taf.TAFChangeForecast;
import fi.fmi.avi.data.taf.TAFForecast;

/**
 * Created by rinne on 24/02/17.
 */
public class AviMessageTestBase {
    protected static final String metar1 =
            "METAR EFHK 012400Z 00000KT 4500 R04R/0500D R15/0600VP1500D R22L/0275N R04L/P1500D BR FEW003 SCT050 14/13 Q1008 " + "TEMPO 2000=";
    protected static final String metar2 = "METAR KORD 201004Z 05008KT 1 1/2SM -DZ BR OVC006 03/03 A2964 RMK AO2 DZB04 P0000 T00330028=";
    protected static final String metar3 =
            "METAR LBBG 041600Z 12012MPS 090V150 1400 R04/P1500N R22/P1500U +SN BKN022 OVC050 M04/M07 Q1020 NOSIG " + "8849//91=";
    protected static final String metar4 =
            "METAR COR EFUT 111115Z 18004KT 150V240 1500 0500N R04R/1500N R15/M0050D R22L/1200N R04L/P1000U SN VV006 M08/M10 " + "Q1023 RESN TEMPO 0900=";
    protected static final String metar5 = "METAR EFTU 011350Z AUTO VRB02KT CAVOK 22/12 Q1008=";
    protected static final String metar6 = "METAR EFHK 010550Z 21018KT 9999 -RA SCT013 BKN025 03/00 Q0988 TEMPO 4000 RASN=";
    protected static final String metar7 = "EGXE 061150Z 03010KT 9999 FEW020 17/11 Q1014 BLU TEMPO 6000 SHRA SCT020 WHT=";
    protected static final String metar8 = "EGXE 061150Z 03010KT 9999 FEW020 17/11 Q1014 BLACKBLU TEMPO 6000 SHRA SCT020 BLACKWHT=";
    protected static final String metar9 =
            "METAR EFHK 111111Z 15008KT 0700 R04R/1500N R15/1000U R22L/1200N R04L/1000VP1500U SN VV006 M08/M10 Q1023 RESN" + "" + " WS ALL RWY TEMPO 0900=";
    protected static final String metar10 =
            "METAR EFHK 111111Z 15008KT 0700 R04R/1500N R15/1000U R22L/1200N R04L/1000VP1500U SN VV006 M08/M10 Q1023 RESN" + " WS RWY04R TEMPO 0900=";
    protected static final String metar11 =
            "METAR EFHK 111111Z 15008KT 0700 R04R/1500N R15/1000U R22L/1200N R04L/1000VP1500U SN VV006 M08/M10 Q1023 " + "WM01/S1 TEMPO TL1530 +SHRA BKN012CB=";
    protected static final String metar12 =
            "METAR EFHK 111111Z 15008KT 0700 R04R/1500N R15/1000U R22L/1200N R04L/1000VP1500U SN M08/M10 Q1023 15CLRD95 " + "54419338=";

    protected static final String metar13 = "METAR EFHK 111111Z 15008KT 0700 R04R/1500N R15/1000U R22L/1200N R04L/1000VP1500U SN M08/M10 Q1023 15//9999=";
    protected static final String metar14 = "METAR EFOU 181750Z AUTO 18007KT 9999 OVC010 02/01 Q1015 R/SNOCLO=";
    protected static final String metar15 = "EFKK 091050Z AUTO 01009KT 340V040 9999 FEW012 BKN046 ///// Q////=";

    protected static final String taf1 =
            "EFVA 271137Z 2712/2812 14015G25KT 8000 -RA SCT020 OVC050 " + "BECMG 2715/2717 5000 -RA BKN007 " + "PROB40 2715/2720 4000 RASN "
                    + "BECMG 2720/2722 16012KT " + "TEMPO 2720/2724 8000 " + "PROB40 2802/2806 3000 RASN BKN004=";
    protected static final String taf2 = "TAF EFAB 190815Z 1909/1915 14008G15MPS 9999 BKN010 BKN015=";
    protected static final String taf3 = "TAF AMD EFAB 191000Z 1909/1915 20008KT CAVOK=";
    protected static final String taf4 = "TAF AMD EFAB 191100Z 1909/1915 CNL=";
    protected static final String taf5 =
            "ENOA 301100Z 3012/3112 29028KT 9999 -SHRA FEW015TCU SCT025 " + "TEMPO 3012/3024 4000 SHRAGS BKN012CB " + "BECMG 3017/3020 25018KT "
                    + "BECMG 3100/3103 17008KT " + "BECMG 3107/3110 23015KT=";
    protected static final String taf6 =
            "TAF EFKU 190830Z 1909/2009 23010KT CAVOK " + "PROB30 TEMPO 1915/1919 7000 SHRA SCT030CB BKN045 " + "BECMG 1923/2001" + " 30010KT=";
    protected static final String taf7 = "TAF EFHK 012350Z NIL=";
    protected static final String taf8 =
            "EFVA 270930Z 2712/2812 14015G25KT 8000 -RA SCT020 OVC050 " + "BECMG 2715/2717 5000 -RA BKN007 " + "PROB40 2715/2720 4000 RASN "
                    + "BECMG 2720/2722 16012KT " + "TEMPO 2720/2724 8000 " + "PROB40 2802/2806 3000 RASN BKN004=";
    protected static final String taf9 =
            "EKSP 301128Z 3012/3112 28020G30KT 9999 BKN025 " + "TEMPO 3012/3017 30025G38KT 5000 SHRA SCT020CB " + "TEMPO 3017/3024 6000 -SHRA BKN012TCU "
                    + "BECMG 3017/3019 26015KT " + "BECMG 3100/3102 18015KT 5000 RA BKN008 " + "TEMPO 3102/3106 15016G26KT 2500 RASN BKN004 "
                    + "BECMG 3106/3108 26018G30KT 9999 -SHRA BKN020=";
    protected static final String taf10 =
            "ESNS 301130Z 3012/3021 15008KT 9999 OVC008 " + "TEMPO 3012/3013 BKN004 " + "TEMPO 3013/3016 4000 -RA BKN004 " + "TEMPO 3016/3018 2400 RASN BKN004 "
                    + "TEMPO 3018/3021 0900 SNRA VV002=";
    protected static final String taf11 =
            "EVRA 301103Z 3012/3112 15013KT 8000 OVC008 " + "TEMPO 3012/3015 14015G26KT 5000 -RA OVC010 " + "FM301500 18012KT 9000 NSW SCT015 BKN020 "
                    + "TEMPO 3017/3103 19020G33KT 3000 -SHRA BKN012CB BKN020=";
    protected static final String taf12 = "EETN 301130Z 3012/3112 14016G26KT 8000 BKN010 OVC015 TXM02/3015 TNM10/3103 " + "TEMPO 3012/3018 3000 RADZ BR OVC004 "
            + "BECMG 3018/3020 BKN008 SCT015CB " + "TEMPO 3102/3112 3000 SHRASN BKN006 BKN015CB " + "BECMG 3104/3106 21016G30KT=";

    private static final double FLOAT_EQUIVALENCE_THRESHOLD = 0.0000000001d;

    protected static void assertMetarEquals(Metar expected, Metar actual) {
        assertEquals("isAutomatedStation", expected.isAutomatedStation(), actual.isAutomatedStation());
        assertEquals("status", expected.getStatus(), actual.getStatus());
        assertEquals("dayOfMonth", expected.getIssueDayOfMonth(), actual.getIssueDayOfMonth());
        assertEquals("hour", expected.getIssueHour(), actual.getIssueHour());
        assertEquals("minute", expected.getIssueMinute(), actual.getIssueMinute());
        assertEquals("timeZone", expected.getIssueTimeZone(), actual.getIssueTimeZone());
        assertEquals("aerodromeDesignator", expected.getAerodromeDesignator(), actual.getAerodromeDesignator());
        assertEquals("CAVOK", expected.isCeilingAndVisibilityOk(), actual.isCeilingAndVisibilityOk());
        assertNumericalMeasureEquals("airTemperature", expected.getAirTemperature(), actual.getAirTemperature());
        assertNumericalMeasureEquals("dewPointTemperature", expected.getDewpointTemperature(), actual.getDewpointTemperature());
        assertNumericalMeasureEquals("altimeterSettingQNH", expected.getAltimeterSettingQNH(), actual.getAltimeterSettingQNH());
        assertObservedSurfaceWindEquals(expected.getSurfaceWind(), actual.getSurfaceWind());
        assertHorizontalVisibilityEquals(expected.getVisibility(), actual.getVisibility());
        assertRunwayVisualRangesEquals(expected.getRunwayVisualRanges(), actual.getRunwayVisualRanges());
        assertEquals("presentWeatherCodes size", expected.getPresentWeatherCodes().size(), actual.getPresentWeatherCodes().size());
        for (int i = 0; i < expected.getPresentWeatherCodes().size(); i++) {
            assertEquals("presentWeatherCode", expected.getPresentWeatherCodes().get(i), actual.getPresentWeatherCodes().get(i));
        }
        assertObservedCloudsEquals(expected.getClouds(), actual.getClouds());
        assertEquals("recentWeatherCodes size", expected.getRecentWeatherCodes().size(), actual.getRecentWeatherCodes().size());
        for (int i = 0; i < expected.getRecentWeatherCodes().size(); i++) {
            assertEquals("recentWeatherCode", expected.getRecentWeatherCodes().get(i), actual.getRecentWeatherCodes().get(i));
        }
        assertWindShearEquals(expected.getWindShear(), actual.getWindShear());
        assertSeaStateEquals(expected.getSeaState(), actual.getSeaState());
        assertRunwayStatesEquals(expected.getRunwayStates(), actual.getRunwayStates());
        assertTrendForecastsEquals(expected.getTrends(), actual.getTrends());
    }

    protected static void assertTAFEquals(TAF expected, TAF actual) {
        assertEquals("status", expected.getStatus(), actual.getStatus());
        assertEquals("issueDayOfMonth", expected.getIssueDayOfMonth(), actual.getIssueDayOfMonth());
        assertEquals("issueHour", expected.getIssueHour(), actual.getIssueHour());
        assertEquals("issueMinute", expected.getIssueMinute(), actual.getIssueMinute());
        assertEquals("timeZone", expected.getIssueTimeZone(), actual.getIssueTimeZone());
        assertEquals("aerodromeDesignator", expected.getAerodromeDesignator(), actual.getAerodromeDesignator());
        assertEquals("validityStartDayOfMonth", expected.getValidityStartDayOfMonth(), actual.getValidityStartDayOfMonth());
        assertEquals("validityStartHour", expected.getValidityStartHour(), actual.getValidityStartHour());
        assertEquals("validityEndDayOfMonth", expected.getValidityEndDayOfMonth(), actual.getValidityEndDayOfMonth());
        assertEquals("validityEndHour", expected.getValidityEndHour(), actual.getValidityEndHour());
        assertTAFBaseForecastEquals(expected.getBaseForecast(), actual.getBaseForecast());
        assertEquals("changeForecasts size", expected.getChangeForecasts().size(), actual.getChangeForecasts().size());
        for (int i = 0; i < expected.getChangeForecasts().size(); i++) {
            assertTAFChangeForecastEquals(expected.getChangeForecasts().get(i), actual.getChangeForecasts().get(i));
        }
        if (expected.getPreviousReportAerodromeDesignator() != null || actual.getPreviousReportAerodromeDesignator() != null) {
            assertEquals("previousReportAerodromeDesignator", expected.getPreviousReportAerodromeDesignator(), actual.getPreviousReportAerodromeDesignator());
            assertEquals("previousReportValidityStartDayOfMonth", expected.getPreviousReportValidityStartDayOfMonth(),
                    actual.getPreviousReportValidityStartDayOfMonth());
            assertEquals("previousReportValidityStartHour", expected.getPreviousReportValidityStartHour(), actual.getPreviousReportValidityStartHour());
            assertEquals("previousReportValidityEndDayOfMonth", expected.getPreviousReportValidityEndDayOfMonth(),
                    actual.getPreviousReportValidityEndDayOfMonth());
            assertEquals("previousReportValidityEndHour", expected.getPreviousReportValidityEndHour(), actual.getPreviousReportValidityEndHour());
        }
    }

    private static void assertObservedSurfaceWindEquals(ObservedSurfaceWind expected, ObservedSurfaceWind actual) {
        if (expected.notMissing() && actual.notMissing()) {
            assertNumericalMeasureEquals("surfaceWind/extremeClockwiseWindDirection", expected.getExtremeClockwiseWindDirection(),
                    actual.getExtremeClockwiseWindDirection());
            assertNumericalMeasureEquals("surfaceWind/extremeCounterClockwiseWindDirection", expected.getExtremeCounterClockwiseWindDirection(),
                    actual.getExtremeCounterClockwiseWindDirection());
            assertNumericalMeasureEquals("surfaceWind/meanWindDirection", expected.getMeanWindDirection(), actual.getMeanWindDirection());
            assertNumericalMeasureEquals("surfaceWind/meanWindSpeed", expected.getMeanWindSpeed(), actual.getMeanWindSpeed());
        } else {
            assertEquals("surfaceWind/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertHorizontalVisibilityEquals(HorizontalVisibility expected, HorizontalVisibility actual) {
        if (expected.notMissing() && actual.notMissing()) {
            assertNumericalMeasureEquals("visibility/prevailingVisibility", expected.getPrevailingVisibility(), actual.getPrevailingVisibility());
            assertEquals("visibility/prevailingVisibilityOperator", expected.getPrevailingVisibilityOperator(), actual.getPrevailingVisibilityOperator());
            assertNumericalMeasureEquals("visibility/minimumVisibility", expected.getMinimumVisibility(), actual.getMinimumVisibility());
            assertNumericalMeasureEquals("visibility/minimumVisibilityDirection", expected.getMinimumVisibilityDirection(),
                    actual.getMinimumVisibilityDirection());
        } else {
            assertEquals("visibility/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertRunwayVisualRangesEquals(List<RunwayVisualRange> expected, List<RunwayVisualRange> actual) {
        assertEquals("runwayVisualRanges size", expected.size(), actual.size());
        RunwayVisualRange expRange = null;
        RunwayVisualRange actRange = null;
        for (int i = 0; i < expected.size(); i++) {
            expRange = expected.get(i);
            actRange = actual.get(i);
            if (expRange.notMissing() && actRange.notMissing()) {
                assertEquals("runwayVisualRange/runwayDirectionDesignator", expRange.getRunwayDirectionDesignator(), actRange.getRunwayDirectionDesignator());
                assertNumericalMeasureEquals("RunwayVisualRange/meanRVR", expRange.getMeanRVR(), actRange.getMeanRVR());
                assertEquals("runwayVisualRange/meanRVROperator", expRange.getMeanRVROperator(), actRange.getMeanRVROperator());
                assertEquals("runwayVisualRange/pastTendency", expRange.getPastTendency(), actRange.getPastTendency());
            } else {
                assertEquals("runwayVisualRange/missingReason", expRange.getMissingReason(), actRange.getMissingReason());
            }
        }
    }

    private static void assertObservedCloudsEquals(ObservedClouds expected, ObservedClouds actual) {
        if (expected.notMissing() && actual.notMissing()) {
            assertEquals("clouds/isAmountAndHeightUnobservableByAutoSystem", expected.isAmountAndHeightUnobservableByAutoSystem(),
                    actual.isAmountAndHeightUnobservableByAutoSystem());
            assertNumericalMeasureEquals("clouds/verticalVisibility", expected.getVerticalVisibility(), actual.getVerticalVisibility());
            assertCloudLayerEquals("clouds/layers", expected.getLayers(), actual.getLayers());
        } else {
            assertEquals("clouds/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertWindShearEquals(WindShear expected, WindShear actual) {
        if (expected.notMissing() && actual.notMissing()) {
            assertEquals("windShear/allRunways", expected.isAllRunways(), actual.isAllRunways());
            assertEquals("windShear/runwayDirectionDesignators size", expected.getRunwayDirectionDesignators().size(),
                    actual.getRunwayDirectionDesignators().size());
            for (int i = 0; i < expected.getRunwayDirectionDesignators().size(); i++) {
                assertEquals("windShear/runwayDirectionDesignator", expected.getRunwayDirectionDesignators().get(i),
                        actual.getRunwayDirectionDesignators().get(i));
            }
        } else {
            assertEquals("windShear/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertSeaStateEquals(SeaState expected, SeaState actual) {
        if (expected.notMissing() && actual.notMissing()) {
            assertNumericalMeasureEquals("seaState/seaSurfaceTemperature", expected.getSeaSurfaceTemperature(), actual.getSeaSurfaceTemperature());
            assertNumericalMeasureEquals("seaState/significantWaveHight", expected.getSignificantWaveHeight(), actual.getSignificantWaveHeight());
            assertEquals("seaState/seaSurfaceState", expected.getSeaSurfaceState(), actual.getSeaSurfaceState());
        } else {
            assertEquals("seaState/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertRunwayStatesEquals(List<RunwayState> expected, List<RunwayState> actual) {
        assertEquals("runwayStates size", expected.size(), actual.size());
        RunwayState expRS = null;
        RunwayState actRS = null;
        for (int i = 0; i < expected.size(); i++) {
            expRS = expected.get(i);
            actRS = actual.get(i);
            if (expRS.notMissing() && actRS.notMissing()) {
                assertEquals("runwayState/allRunways", expRS.isAllRunways(), actRS.isAllRunways());
                assertEquals("runwayState/isCleared match", expRS.isCleared(), actRS.isCleared());
                assertEquals("runwayState/isEstimatedSurfaceFrictionUnreliable", expRS.isEstimatedSurfaceFrictionUnreliable(),
                        actRS.isEstimatedSurfaceFrictionUnreliable());
                assertEquals("runwayState/isSnowClosure", expRS.isSnowClosure(), actRS.isSnowClosure());
                assertEquals("runwayState/runwayDirectionDesignator", expRS.getRunwayDirectionDesignator(), actRS.getRunwayDirectionDesignator());
                assertEquals("runwayState/runwayDeposit", expRS.getDeposit(), actRS.getDeposit());
                assertEquals("runwayState/runwayContamination", expRS.getContamination(), actRS.getContamination());
                assertNumericalMeasureEquals("RunwayState/depthOfDeposit", expRS.getDepthOfDeposit(), actRS.getDepthOfDeposit());
                assertTrue("runwayState/estimatedSurfaceFriction close enough",
                        Math.abs(expRS.getEstimatedSurfaceFriction() - actRS.getEstimatedSurfaceFriction()) < FLOAT_EQUIVALENCE_THRESHOLD);

            } else {
                assertEquals("runwayState/missingReason", expRS.getMissingReason(), actRS.getMissingReason());
            }
        }
    }

    private static void assertTrendForecastsEquals(List<TrendForecast> expected, List<TrendForecast> actual) {
        assertEquals("trends size", expected.size(), actual.size());
        TrendForecast expTrend = null;
        TrendForecast actTrend = null;
        TrendTimeGroups expTimeGroups = null;
        TrendTimeGroups actTimeGroups = null;
        TrendForecastSurfaceWind expFctWind = null;
        TrendForecastSurfaceWind actFctWind = null;
        CloudForecast expFctCloud = null;
        CloudForecast actFctCloud = null;
        for (int i = 0; i < expected.size(); i++) {
            expTrend = expected.get(i);
            actTrend = actual.get(i);
            if (expTrend.notMissing() && actTrend.notMissing()) {
                expTimeGroups = expTrend.getParsedTimeGroups();
                actTimeGroups = actTrend.getParsedTimeGroups();
                assertEquals("trend/timeGroups/fromHour", expTimeGroups.getFromHour(), actTimeGroups.getFromHour());
                assertEquals("trend/timeGroups/fromMinute", expTimeGroups.getFromMinute(), actTimeGroups.getFromMinute());
                assertEquals("trend/timeGroups/toHour", expTimeGroups.getToHour(), actTimeGroups.getToHour());
                assertEquals("trend/timeGroups/toMinute", expTimeGroups.getToMinute(), actTimeGroups.getToMinute());
                assertEquals("trend/timeGroups/isSingular", expTimeGroups.isSingular(), actTimeGroups.isSingular());

                assertEquals("trend/CAVOK", expTrend.isCeilingAndVisibilityOk(), actTrend.isCeilingAndVisibilityOk());
                assertEquals("trend/changeIndicator", expTrend.getChangeIndicator(), actTrend.getChangeIndicator());

                assertNumericalMeasureEquals("trend/prevailingVisibility", expTrend.getPrevailingVisibility(), actTrend.getPrevailingVisibility());
                assertEquals("trend/prevailingVisibilityOperator", expTrend.getPrevailingVisibilityOperator(), actTrend.getPrevailingVisibilityOperator());

                expFctWind = expTrend.getSurfaceWind();
                actFctWind = actTrend.getSurfaceWind();
                assertTrendForecastWindEquals("trend/surfaceWind", expFctWind, actFctWind);

                assertEquals("trend/forecastWeather size", expTrend.getForecastWeather().size(), actTrend.getForecastWeather().size());
                for (int j = 0; j < expTrend.getForecastWeather().size(); j++) {
                    assertEquals("trend/forecastWeather", expTrend.getForecastWeather().get(j), actTrend.getForecastWeather().get(j));
                }

                expFctCloud = expTrend.getCloud();
                actFctCloud = actTrend.getCloud();
                assertCloudForecastEquals("trend/cloud", expFctCloud, actFctCloud);

            } else {
                assertEquals("trend/missingReason", expTrend.getMissingReason(), actTrend.getMissingReason());
            }
        }
    }

    private static void assertTAFBaseForecastEquals(TAFBaseForecast expected, TAFBaseForecast actual) {
        assertTAFForecastEquals("baseForecast", expected, actual);
        TAFAirTemperatureForecast expAir = null;
        TAFAirTemperatureForecast actAir = null;
        assertEquals("baseForecast/temperatures size", expected.getTemperatures().size(), actual.getTemperatures().size());
        for (int i = 0; i < expected.getTemperatures().size(); i++) {
            expAir = expected.getTemperatures().get(i);
            actAir = actual.getTemperatures().get(i);
            assertNumericalMeasureEquals("baseForecast/temperature/maxTemperature", expAir.getMinTemperature(), actAir.getMinTemperature());
            assertEquals("baseForecast/temperature/maxTemperatureDayOfMonth", expAir.getMinTemperatureDayOfMonth(), actAir.getMinTemperatureDayOfMonth());
            assertEquals("baseForecast/temperature/maxTemperatureHour", expAir.getMinTemperatureHour(), actAir.getMinTemperatureHour());
            assertNumericalMeasureEquals("temperature/minTemperature", expAir.getMinTemperature(), actAir.getMinTemperature());
            assertEquals("baseForecast/temperature/minTemperatureDayOfMonth", expAir.getMinTemperatureDayOfMonth(), actAir.getMinTemperatureDayOfMonth());
            assertEquals("baseForecast/temperature/minTemperatureHour", expAir.getMinTemperatureHour(), actAir.getMinTemperatureHour());
        }
    }

    private static void assertTAFChangeForecastEquals(TAFChangeForecast expected, TAFChangeForecast actual) {
        assertTAFForecastEquals("changeForecast", expected, actual);
        assertEquals("changeForecast/changeIndicator", expected.getChangeIndicator(), actual.getChangeIndicator());
        assertEquals("changeForecast/validityStartDayOfMonth", expected.getValidityStartDayOfMonth(), actual.getValidityStartDayOfMonth());
        assertEquals("changeForecast/validityStartHour", expected.getValidityStartHour(), actual.getValidityStartHour());
        assertEquals("changeForecast/validityStartMinute", expected.getValidityStartMinute(), actual.getValidityStartMinute());
        assertEquals("changeForecast/validityEndDayOfMonth", expected.getValidityEndDayOfMonth(), actual.getValidityEndDayOfMonth());
        assertEquals("changeForecast/validityEndHour", expected.getValidityEndHour(), actual.getValidityEndHour());
    }

    private static void assertTAFForecastEquals(String message, TAFForecast expected, TAFForecast actual) {
        assertEquals(message + "/CAVOK", expected.isCeilingAndVisibilityOk(), actual.isCeilingAndVisibilityOk());
        assertNumericalMeasureEquals(message + "/prevailingVisibility", expected.getPrevailingVisibility(), actual.getPrevailingVisibility());
        assertEquals(message + "/prevailingVisibilityOperator", expected.getPrevailingVisibilityOperator(), actual.getPrevailingVisibilityOperator());

        assertTrendForecastWindEquals(message + "/surfaceWind", expected.getSurfaceWind(), actual.getSurfaceWind());
        assertEquals(message + "/surfaceWind variable", expected.getSurfaceWind().isVariableDirection(), actual.getSurfaceWind().isVariableDirection());
        assertEquals(message + "/forecastWeather size", expected.getForecastWeather().size(), actual.getForecastWeather().size());
        for (int i = 0; i < expected.getForecastWeather().size(); i++) {
            assertEquals(message + "/forecastWeather", expected.getForecastWeather().get(i), actual.getForecastWeather().get(i));
        }
        assertCloudForecastEquals(message + "/cloud", expected.getCloud(), actual.getCloud());
    }

    private static void assertNumericalMeasureEquals(String message, NumericMeasure expected, NumericMeasure actual) {
        assertEquals(message + "/missingReason", expected.getMissingReason(), actual.getMissingReason());
        assertTrue(message + "/value close enough", Math.abs(expected.getValue() - actual.getValue()) < FLOAT_EQUIVALENCE_THRESHOLD);
        assertEquals(message + "/uom", expected.getUom(), actual.getUom());
    }

    private static void assertTrendForecastWindEquals(String message, TrendForecastSurfaceWind expected, TrendForecastSurfaceWind actual) {
        if (expected.notMissing() && actual.notMissing()) {
            assertNumericalMeasureEquals(message + "/meanWindDirection", expected.getMeanWindDirection(), actual.getMeanWindDirection());
            assertNumericalMeasureEquals(message + "/meanWindSpeed", expected.getMeanWindSpeed(), actual.getMeanWindSpeed());
            assertNumericalMeasureEquals(message + "/windGust", expected.getWindGust(), actual.getWindGust());
        } else {
            assertEquals(message + "/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertCloudForecastEquals(String message, CloudForecast expected, CloudForecast actual) {
        if (expected.notMissing() && expected.notMissing()) {
            assertNumericalMeasureEquals(message + "/verticalVisibility", expected.getVerticalVisibility(), actual.getVerticalVisibility());
            assertCloudLayerEquals(message + "/layers", expected.getLayers(), actual.getLayers());
        } else {
            assertEquals(message + "/missingReason", expected.getMissingReason(), actual.getMissingReason());
        }
    }

    private static void assertCloudLayerEquals(String message, List<CloudLayer> expected, List<CloudLayer> actual) {
        assertEquals(message + " size", expected.size(), actual.size());
        CloudLayer expLayer = null;
        CloudLayer actLayer = null;
        for (int i = 0; i < expected.size(); i++) {
            expLayer = expected.get(i);
            actLayer = actual.get(i);
            if (expLayer.notMissing() && actLayer.notMissing()) {
                assertEquals(message + "/amount", expLayer.getAmount(), actLayer.getAmount());
                assertNumericalMeasureEquals(message + "/base", expLayer.getBase(), actLayer.getBase());
                assertEquals(message + "/cloudType", expLayer.getCloudType(), actLayer.getCloudType());
            } else {
                assertEquals(message + "/missingReason", expLayer.getMissingReason(), actLayer.getMissingReason());
            }
        }
    }

    protected static <T extends AviationWeatherMessage> T readFromJSON(String fileName, Class<T> clz) throws IOException {
        T retval = null;
        ObjectMapper om = new ObjectMapper();
        InputStream is = AviMessageParserTest.class.getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            retval = om.readValue(is, clz);
        } else {
            throw new FileNotFoundException("Resource '" + fileName + "' could not be loaded");
        }
        return retval;
    }
}
