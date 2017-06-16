package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RUNWAY;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fi.fmi.avi.data.AviationCodeListUser.RunwayDeposit;
import fi.fmi.avi.data.AviationCodeListUser;
import fi.fmi.avi.data.AviationCodeListUser.BreakingAction;
import fi.fmi.avi.data.AviationCodeListUser.RunwayContamination;
import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.data.NumericMeasure;
import fi.fmi.avi.data.metar.Metar;
import fi.fmi.avi.parser.ConversionHints;
import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.SerializingException;
import fi.fmi.avi.parser.impl.lexer.FactoryBasedReconstructor;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;
import fi.fmi.avi.parser.impl.lexer.RecognizingAviMessageTokenLexer.RelationalOperator;

/**
 * Created by rinne on 10/02/17.
 */
public class RunwayState extends RegexMatchingLexemeVisitor {
    public enum RunwayStateDeposit {
        CLEAR_AND_DRY('0'),
        DAMP('1'),
        WET('2'),
        RIME_OR_FROST_COVERED('3'),
        DRY_SNOW('4'),
        WET_SNOW('5'),
        SLUSH('6'),
        ICE('7'),
        COMPACTED_OR_ROLLED_SNOW('8'),
        FROZEN_RUTS_OR_RIDGES('9'),
        NOT_REPORTED('/');

        private char code;

        RunwayStateDeposit(final char code) {
            this.code = code;
        }

        public static RunwayStateDeposit forCode(final char code) {
            for (RunwayStateDeposit w : values()) {
                if (w.code == code) {
                    return w;
                }
            }
            return null;
        }

    }

    public enum RunwayStateContamination {
        LESS_OR_EQUAL_TO_10PCT('1'), FROM_11_TO_25PCT('2'), FROM_26_TO_50PCT('5'), FROM_51_TO_100PCT('9'), NOT_REPORTED('/');

        private char code;

        RunwayStateContamination(final char code) {
            this.code = code;
        }

        public static RunwayStateContamination forCode(final char code) {
            for (RunwayStateContamination w : values()) {
                if (w.code == code) {
                    return w;
                }
            }
            return null;
        }

    }

    public enum RunwayStateReportType {
        SNOW_CLOSURE,
        DEPOSITS,
        CONTAMINATION,
        DEPTH_OF_DEPOSIT,
        UNIT_OF_DEPOSIT,
        FRICTION_COEFFICIENT,
        BREAKING_ACTION,
        REPETITION,
        ALL_RUNWAYS,
        CLEARED,
        DEPTH_MODIFIER;
    }

    public enum RunwayStateReportSpecialValue {
        NOT_MEASURABLE, MEASUREMENT_UNRELIABLE, MORE_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, RUNWAY_NOT_OPERATIONAL,
    }

    public enum BreakingAction {
        POOR(91), MEDIUM_POOR(92), MEDIUM(93), MEDIUM_GOOD(94), GOOD(95);

        private int code;

        BreakingAction(final int code) {
            this.code = code;
        }

        public static BreakingAction forCode(final int code) {
            for (BreakingAction w : values()) {
                if (w.code == code) {
                    return w;
                }
            }
            return null;
        }
    }


    private static final Map<RunwayStateDeposit, RunwayDeposit> runwayStateDepositToAPI;
    private static final Map<RunwayDeposit, RunwayStateDeposit> apiToRunwayStateDeposit;

    private static final Map<RunwayStateContamination, RunwayContamination> runwayStateContaminationToAPI;
    private static final Map<RunwayContamination, RunwayStateContamination> apiToRunwayStateContamination;
    
    private static final Map<BreakingAction, AviationCodeListUser.BreakingAction> breakingActionToAPI;
    private static final Map<AviationCodeListUser.BreakingAction, BreakingAction> apiTobreakingAction;

    static {
    	{
	    	BiMap<RunwayStateDeposit, RunwayDeposit> tmp = HashBiMap.create();
	    	
	    	tmp.put(RunwayStateDeposit.CLEAR_AND_DRY, RunwayDeposit.CLEAR_AND_DRY);
	    	tmp.put(RunwayStateDeposit.COMPACTED_OR_ROLLED_SNOW, RunwayDeposit.COMPACT_OR_ROLLED_SNOW);
	    	tmp.put(RunwayStateDeposit.DAMP, RunwayDeposit.DAMP);
	    	tmp.put(RunwayStateDeposit.DRY_SNOW, RunwayDeposit.DRY_SNOW);
	    	tmp.put(RunwayStateDeposit.FROZEN_RUTS_OR_RIDGES, RunwayDeposit.FROZEN_RUTS_OR_RIDGES);
	    	tmp.put(RunwayStateDeposit.ICE, RunwayDeposit.ICE);
	    	tmp.put(RunwayStateDeposit.NOT_REPORTED, RunwayDeposit.MISSING_OR_NOT_REPORTED);
	    	tmp.put(RunwayStateDeposit.RIME_OR_FROST_COVERED, RunwayDeposit.RIME_AND_FROST_COVERED);
	    	tmp.put(RunwayStateDeposit.SLUSH, RunwayDeposit.SLUSH);
	    	tmp.put(RunwayStateDeposit.WET, RunwayDeposit.WET_WITH_WATER_PATCHES);
	    	tmp.put(RunwayStateDeposit.WET_SNOW, RunwayDeposit.WET_SNOW);
	    	
	    	runwayStateDepositToAPI = Collections.unmodifiableMap(tmp);
	    	apiToRunwayStateDeposit = Collections.unmodifiableMap(tmp.inverse());
    	}
    	
    	{
	    	BiMap<RunwayStateContamination, RunwayContamination> tmp = HashBiMap.create();
	    	
	    	tmp.put(RunwayStateContamination.LESS_OR_EQUAL_TO_10PCT, RunwayContamination.PCT_COVERED_LESS_THAN_10);
	    	tmp.put(RunwayStateContamination.FROM_11_TO_25PCT, RunwayContamination.PCT_COVERED_11_25);
	    	tmp.put(RunwayStateContamination.FROM_26_TO_50PCT, RunwayContamination.PCT_COVERED_26_50);
	    	tmp.put(RunwayStateContamination.FROM_51_TO_100PCT, RunwayContamination.PCT_COVERED_51_100);
	    	tmp.put(RunwayStateContamination.NOT_REPORTED, RunwayContamination.MISSING_OR_NOT_REPORTED);

	    	runwayStateContaminationToAPI = Collections.unmodifiableMap(tmp);
	    	apiToRunwayStateContamination = Collections.unmodifiableMap(tmp.inverse());
    	}
    	
    	{
    		BiMap<BreakingAction, AviationCodeListUser.BreakingAction> tmp = HashBiMap.create();
    		
    		tmp.put(BreakingAction.POOR, AviationCodeListUser.BreakingAction.POOR);
    		tmp.put(BreakingAction.MEDIUM_POOR, AviationCodeListUser.BreakingAction.MEDIUM_POOR);
    		tmp.put(BreakingAction.MEDIUM, AviationCodeListUser.BreakingAction.MEDIUM);
    		tmp.put(BreakingAction.MEDIUM_GOOD, AviationCodeListUser.BreakingAction.MEDIUM_GOOD);
    		tmp.put(BreakingAction.GOOD, AviationCodeListUser.BreakingAction.GOOD);
    		
    		breakingActionToAPI = Collections.unmodifiableMap(tmp);
    		apiTobreakingAction = Collections.unmodifiableMap(tmp.inverse());
    	}
    }
    
    
    
    public RunwayState(final Priority prio) {
        super("^R?([0-9]{2})((([0-9/])([1259/])([0-9]{2}|//))|(CLRD))([0-9]{2}|//)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints) {

        HashMap<RunwayStateReportType, Object> values = new HashMap<RunwayStateReportType, Object>();
        Lexeme.Status status = Lexeme.Status.OK;
        String msg = null;

        Object runwayDesignation = getRunwayDesignation(Integer.parseInt(match.group(1)));
        if (runwayDesignation == RunwayStateReportType.REPETITION) {
            values.put(RunwayStateReportType.REPETITION, Boolean.TRUE);
        } else if (runwayDesignation == RunwayStateReportType.ALL_RUNWAYS) {
            values.put(RunwayStateReportType.ALL_RUNWAYS, Boolean.TRUE);
        }

        if (match.group(4) != null && match.group(5) != null && match.group(6) != null) {
            values.put(RunwayStateReportType.DEPOSITS, RunwayStateDeposit.forCode(match.group(4).charAt(0)));
            values.put(RunwayStateReportType.CONTAMINATION, RunwayStateContamination.forCode(match.group(5).charAt(0)));

            String depthCode = match.group(6);
            if ("00".equals(depthCode)) {
                values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(1));
                values.put(RunwayStateReportType.UNIT_OF_DEPOSIT, "mm");
                values.put(RunwayStateReportType.DEPTH_MODIFIER, RunwayStateReportSpecialValue.LESS_THAN_OR_EQUAL);
            } else if ("91".equals(depthCode)) {
                status = Lexeme.Status.SYNTAX_ERROR;
                msg = "Illegal depth of deposit: 91";
            } else if ("//".equals(depthCode)) {
                values.put(RunwayStateReportType.DEPTH_MODIFIER, RunwayStateReportSpecialValue.NOT_MEASURABLE);
            } else if ("99".equals(depthCode)) {
                values.put(RunwayStateReportType.DEPTH_MODIFIER, RunwayStateReportSpecialValue.RUNWAY_NOT_OPERATIONAL);
            } else {
                int depthCodeNumber = Integer.parseInt(depthCode);
                if (depthCodeNumber <= 90) {
                    values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, depthCodeNumber);
                    values.put(RunwayStateReportType.UNIT_OF_DEPOSIT, "mm");
                } else if (depthCodeNumber <= 98) {
                    values.put(RunwayStateReportType.UNIT_OF_DEPOSIT, "cm");
                    switch (depthCodeNumber) {
                        case 92:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(10));
                            break;
                        case 93:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(15));
                            break;
                        case 94:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(20));
                            break;
                        case 95:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(25));
                            break;
                        case 96:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(30));
                            break;
                        case 97:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(35));
                            break;
                        case 98:
                            values.put(RunwayStateReportType.DEPTH_OF_DEPOSIT, Integer.valueOf(40));
                            values.put(RunwayStateReportType.DEPTH_MODIFIER, RunwayStateReportSpecialValue.MORE_THAN_OR_EQUAL);
                            break;
                    }
                }
            }
        }
        if (match.group(7) != null) {
            values.put(RunwayStateReportType.CLEARED, Boolean.TRUE);
        }
        try {
            appendFrictionCoeffOrBreakingAction(match.group(8), values);
        } catch (IllegalArgumentException iae) {
            status = Lexeme.Status.SYNTAX_ERROR;
            msg = iae.getMessage();
        }
        token.identify(RUNWAY_STATE, status, msg);
        token.setParsedValue(VALUE, values);
        if (runwayDesignation instanceof String) {
        	token.setParsedValue(RUNWAY, runwayDesignation);
        }
    }

    private static Object getRunwayDesignation(final int coded) {
        Object retval = null;
        if (coded == 99) {
            retval = RunwayStateReportType.REPETITION;
        } else if (coded == 88) {
            retval = RunwayStateReportType.ALL_RUNWAYS;
        } else if (coded > 50) {
            retval = String.format("%02dR", coded - 50);
        } else {
        	retval = String.format("%02d", coded);
        }
        return retval;
    }

    public static RunwayDeposit convertRunwayStateDepositToAPI(RunwayStateDeposit deposit) {
    	return runwayStateDepositToAPI.get(deposit);
	}

    public static RunwayStateDeposit convertAPIToRunwayStateDeposit(RunwayDeposit deposit) {
    	return apiToRunwayStateDeposit.get(deposit);
	}
    
    public static RunwayContamination convertRunwayStateContaminationToAPI(RunwayStateContamination contamination) {
    	return runwayStateContaminationToAPI.get(contamination);
    }
    
    public static RunwayStateContamination convertAPIToRunwayStateContamination(RunwayContamination contamination) {
    	return apiToRunwayStateContamination.get(contamination);
    }
    
    public static AviationCodeListUser.BreakingAction convertBreakingActionToAPI(BreakingAction breakingAction) {
    	return breakingActionToAPI.get(breakingAction);
    }
    
    public static BreakingAction convertAPIToBreakingAction(AviationCodeListUser.BreakingAction breakingAction) {
    	return apiTobreakingAction.get(breakingAction);
    }

    
    private static void appendFrictionCoeffOrBreakingAction(final String coded, HashMap<RunwayStateReportType, Object> values) throws IllegalArgumentException {
        if ("//".equals(coded)) {
            values.put(RunwayStateReportType.FRICTION_COEFFICIENT, RunwayStateReportSpecialValue.RUNWAY_NOT_OPERATIONAL);
            values.put(RunwayStateReportType.BREAKING_ACTION, RunwayStateReportSpecialValue.RUNWAY_NOT_OPERATIONAL);
        } else {
            int fcbaValue = Integer.parseInt(coded);
            if (fcbaValue == 99) {
                values.put(RunwayStateReportType.FRICTION_COEFFICIENT, RunwayStateReportSpecialValue.MEASUREMENT_UNRELIABLE);
            } else if (fcbaValue < 91) {
                values.put(RunwayStateReportType.FRICTION_COEFFICIENT, fcbaValue);
            } else {
                BreakingAction ba = BreakingAction.forCode(fcbaValue);
                if (ba != null) {
                    values.put(RunwayStateReportType.BREAKING_ACTION, ba);
                } else {
                    throw new IllegalArgumentException("Illegal breaking action code " + fcbaValue);
                }
            }
        }
    }
    
    public static class Reconstructor extends FactoryBasedReconstructor {
    	@Override
    	public <T extends AviationWeatherMessage> Lexeme getAsLexeme(T msg, Class<T> clz, ConversionHints hints,
    			Object... specifier) throws SerializingException {
    		Lexeme retval = null;
    		fi.fmi.avi.data.metar.RunwayState state = null;
    		
            if (Metar.class.isAssignableFrom(clz)) {
                state = getAs(specifier, fi.fmi.avi.data.metar.RunwayState.class);
            }
            
            if (state != null) {
            	StringBuilder builder = new StringBuilder();
            	
            	// Runway designator
            	builder.append(getRunwayDesignator(state));
            	
            	if (state.isCleared()) {
            		builder.append("CLRD");
            	} else {
	            	// Deposit
	            	RunwayStateDeposit deposit = convertAPIToRunwayStateDeposit(state.getDeposit());
	            	if (deposit == null) {
	            		throw new SerializingException("RunwayState deposit ("+state.getDeposit()+") missing or unable to convert it");
	            	}
	            	builder.append(deposit.code);
	            	
	            	// Contamination
	            	RunwayStateContamination contamination = convertAPIToRunwayStateContamination(state.getContamination());
	            	if (contamination == null) {
	            		throw new SerializingException("RunwayState contamination ("+state.getContamination()+") missing or unable to convert it");
	            	}
	            	builder.append(contamination.code);
	            	
	            	// Depth of deposit
	            	builder.append(getDepthOfDeposit(state));
	            	
            	}
            	
            	// Friction coefficient - appending it after CLRD is not 100% as spec, but we have real world test cases where this is done
            	builder.append(getFrictionCoefficient(state));
            	
            	retval = this.createLexeme(builder.toString(), RUNWAY_STATE);
            }
            
            return retval;
    	}

		private String getFrictionCoefficient(fi.fmi.avi.data.metar.RunwayState state) throws SerializingException {
			fi.fmi.avi.data.AviationCodeListUser.BreakingAction action = state.getBreakingAction();
			Double friction = state.getEstimatedSurfaceFriction();
			
			if (state.isRunwayNotOperational()) {
				return "//";
			}
			
			if (state.isEstimatedSurfaceFrictionUnreliable()) {
				return "99";
			}
			
			if (action != null) {
				BreakingAction tmp = convertAPIToBreakingAction(action);
				if (tmp == null) {
					throw new SerializingException("RunwayState has unknown breaking action "+action);
				}
				return String.format("%02d", tmp.code);
			}
			
			if (friction == null) {
				throw new SerializingException("RunwayState estimated surface friction missing");
			}
			
			int value = friction.intValue();
			if (value < 0 || value >= 91) {
				throw new SerializingException("RunwayState friction coefficient "+friction+" is out of bounds (should be between 0 and 90)");
			}
			
			return String.format("%02d", value);
		}

		private String getDepthOfDeposit(fi.fmi.avi.data.metar.RunwayState state) throws SerializingException {
			NumericMeasure measure = state.getDepthOfDeposit();
			fi.fmi.avi.data.AviationCodeListUser.RelationalOperator operator = state.getDepthOperator();
			
			if (state.isDepthNotMeasurable()) {
				return "//";
			}

			if (measure == null) {
				throw new SerializingException("Depth is measurable, but depthOfDeposit is null. Unable to serialize");
			}
			
			boolean millimeters;
			if ("mm".equals(measure.getUom())) {
				millimeters = true;
			} else if ("cm".equals(measure.getUom())) {
				millimeters = false;
			} else {
				throw new SerializingException("Unit of measure for depth of deposit can only be mm or cm");
			}
			
			int value = measure.getValue().intValue();
			if (operator == null) {
				if (millimeters) {
					if (value < 0 || value > 90) {
						throw new SerializingException("Depth of deposit mm depth "+value+" is out of bounds. It should be between 0 and 90");
					}
					return String.format("%02d", value);
				} else {
					String ret;
					switch (value) {
					case 10: ret = "92"; break;
					case 15: ret = "93"; break;
					case 20: ret = "94"; break;
					case 25: ret = "95"; break;
					case 30: ret = "96"; break;
					case 35: ret = "97"; break;
					default:
						throw new SerializingException("Depth of deposit in cm must be 10,15,20,25,30,35 or ABOVE 40");
					}
					return ret;
				}
			} else if (operator == fi.fmi.avi.data.AviationCodeListUser.RelationalOperator.BELOW) {
				if ( (millimeters && measure.getValue() <= 1.0) || measure.getValue() <= 0.1) {
					return "00";
				}
				throw new SerializingException("Depth of deposit operator is BELOW, but measure is not 1mm or under, but: "+measure);
			} else if (operator == fi.fmi.avi.data.AviationCodeListUser.RelationalOperator.ABOVE) {
				if (millimeters && value < 400) {
					throw new SerializingException("Depth of deposit with operator ABOVE needs to be 400mm or more");
				} else if (!millimeters && value < 40) {
					throw new SerializingException("Depth of deposit with operator ABOVE needs to be 40cm or more");
				}
				return "98";
			} else {
				
				throw new SerializingException("Unknown depth of deposit operator "+operator);
			}
		}

		private String getRunwayDesignator(fi.fmi.avi.data.metar.RunwayState state) throws SerializingException {
			String runwayDesignator;
			if (state.isRepetition()) {
				runwayDesignator = "99";
			} else if (state.isAllRunways()) {
				runwayDesignator = "88";
			} else {
				String designator = state.getRunwayDirectionDesignator();
				if (designator == null || !designator.matches("[0-9][0-9]R?")) {
					throw new SerializingException("Illegal runway designator in RunwayState "+designator);
				}
				boolean rightSide = designator.endsWith("R");
				if (rightSide) {
					designator = designator.substring(0, designator.length()-1);
				}
				
				int code = Integer.parseInt(designator);
				if (code >= 50 || code < 0) {
					throw new SerializingException("Illegal runway designator code "+code+" in RunwayState");
				}
				
				if (rightSide) {
					code += 50;
				}
				runwayDesignator = String.format("%02d", code);
			}
			return runwayDesignator;
		}
    }
}
