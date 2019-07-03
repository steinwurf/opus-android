package com.steinwurf.opus;


/**
 * Audio bandwidth
 */
public enum Bandwidth
{
    /**
     * Auto/default setting
     */
    AUTO(-1000),
    /**
     *  4 kHz bandpass
     */
    NARROW_BAND(1101),
    /**
     *  6 kHz bandpass
     */
    MEDIUM_BAND(1102),
    /**
     *  8 kHz bandpass
     */
    WIDE_BAND(1103),
    /**
     * 12 kHz bandpass
     */
    SUPER_WIDE_BAND(1104),
    /**
     * 20 kHz bandpass
     */
    FULL_BAND(1105);

    final int value;

    Bandwidth(int value) {
        this.value = value;
    }

    static Bandwidth get(int value)
    {
        switch (value)
        {
            case -1000:
                return AUTO;
            case 1101:
                return NARROW_BAND;
            case 1102:
                return MEDIUM_BAND;
            case 1103:
                return WIDE_BAND;
            case 1104:
                return SUPER_WIDE_BAND;
            case 1105:
                return FULL_BAND;
        }
        return null;
    }
}