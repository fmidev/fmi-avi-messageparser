package fi.fmi.avi.parser.impl.lexer.token;

import static fi.fmi.avi.parser.Lexeme.Identity.RUNWAY_STATE;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.RUNWAY;
import static fi.fmi.avi.parser.Lexeme.ParsedValueName.VALUE;

import java.util.HashMap;
import java.util.regex.Matcher;

import fi.fmi.avi.parser.Lexeme;
import fi.fmi.avi.parser.ParsingHints;
import fi.fmi.avi.parser.impl.lexer.RegexMatchingLexemeVisitor;

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

    public RunwayState(final Priority prio) {
        super("^R?([0-9]{2})((([0-9/])([1259/])([0-9]{2}|//))|(CLRD))([0-9]{2}|//)$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ParsingHints hints) {

        HashMap<RunwayStateReportType, Object> values = new HashMap<RunwayStateReportType, Object>();
        Lexeme.Status status = Lexeme.Status.OK;
        String msg = null;

        Object runwayDesignation = getRunwayDesignation(Integer.parseInt(match.group(1)));
        if (runwayDesignation == RunwayStateReportType.REPETITION) {
            values.put(RunwayStateReportType.REPETITION, Boolean.TRUE);
        } else if (runwayDesignation == RunwayStateReportType.ALL_RUNWAYS) {
            values.put(RunwayStateReportType.ALL_RUNWAYS, Boolean.TRUE);
        } else {
            token.setParsedValue(RUNWAY, runwayDesignation);
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
        token.setParsedValue(VALUE, values);
        token.identify(RUNWAY_STATE, status, msg);
    }

    private static Object getRunwayDesignation(final int coded) {
        Object retval = null;
        int runwayNumber = -1;
        if (coded == 99) {
            retval = RunwayStateReportType.REPETITION;
        } else if (coded == 88) {
            retval = RunwayStateReportType.ALL_RUNWAYS;
        } else if (coded > 50) {
            runwayNumber = coded - 50;
            if (runwayNumber < 10) {
                retval = "0" + runwayNumber + "R";
            } else {
                retval = "" + runwayNumber + "R";
            }
        } else {
            runwayNumber = coded;
            if (runwayNumber < 10) {
                retval = "0" + runwayNumber;
            } else {
                retval = "" + runwayNumber;
            }
        }
        return retval;
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
}
