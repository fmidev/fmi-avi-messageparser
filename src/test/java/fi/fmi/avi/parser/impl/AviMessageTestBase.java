package fi.fmi.avi.parser.impl;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.ReflectionComparatorFactory;
import org.unitils.reflectionassert.comparator.Comparator;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.data.taf.TAF;

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
    protected static final String metar16 = "METAR EFTU 011350Z AUTO VRB02KT 9999 ////// 22/12 Q1008=";
    protected static final String metar17 = "METAR KORD 201004Z 05008KT 1 1/2SM -DZ BR OVC006 03/03 04/54 A2964=";

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

    private static Difference deepCompareObjects(Object expected, Object actual) {
    	
    	// Use anonymous class to call protected member function
    	LinkedList<Comparator> comparatorChain = (new ReflectionComparatorFactory() {
	    		LinkedList<Comparator> createBaseComparators() {
	    			return new LinkedList<Comparator>(getComparatorChain(Collections.emptySet()));
	    		}
	    	}).createBaseComparators();
    	
    	// Add lenient collection comparator ([] == null) as first-in-chain
    	comparatorChain.addFirst(new Comparator() {
			
			@Override
			public Difference compare(Object left, Object right, boolean onlyFirstDifference,
					ReflectionComparator reflectionComparator) {
				Collection<?> coll = (Collection<?>)left;
				if (coll == null) {
					coll = (Collection<?>)right;
				}
				
				if (coll.size() == 0) {
					return null;
				}
				
				return new Difference("Null list does not match a non-empty list", left, right);
			}
			
			@Override
			public boolean canCompare(Object left, Object right) {
				return
					(left == null && right instanceof Collection<?>) || 
					(right == null && left instanceof Collection<?>);
			}
		});
    	
    	// Add double comparator with specified accuracy as first-in-chain
    	comparatorChain.addFirst(new Comparator() {
    		@Override
    		public Difference compare(Object left, Object right, boolean onlyFirstDifference,
    				ReflectionComparator reflectionComparator) {
    			double diff = Math.abs( ((Double)left) - ((Double)right));
    			if (diff >= FLOAT_EQUIVALENCE_THRESHOLD) {
    				return new Difference("Floating point values differ more than set threshold", left, right);
    			}
    			
    			return null;
    		}
    		
    		@Override
    		public boolean canCompare(Object left, Object right) {
    			return left instanceof Double && right instanceof Double;
    		}
    	
    	});
    	
    	
    	ReflectionComparator reflectionComparator = new ReflectionComparator(comparatorChain);
    	return reflectionComparator.getDifference(expected, actual);
    }
    
    protected static void assertMetarEquals(Metar expected, Metar actual) {
    	Difference diff = deepCompareObjects(expected, actual);
    	if (diff != null) {
            StringBuilder failureMessage = new StringBuilder();
            failureMessage.append("METAR objects are not equivalent\n");
            failureMessage.append(new DefaultDifferenceReport().createReport(diff));
            fail(failureMessage.toString());
    	}
    }

    protected static void assertTAFEquals(TAF expected, TAF actual) {
    	Difference diff = deepCompareObjects(expected, actual);
    	if (diff != null) {
            StringBuilder failureMessage = new StringBuilder();
            failureMessage.append("TAF objects are not equivalent\n");
            failureMessage.append(new DefaultDifferenceReport().createReport(diff));
            fail(failureMessage.toString());
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
