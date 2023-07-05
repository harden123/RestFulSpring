package tool.utils;

public class StringUtils {
	
	  /**
     * The empty String {@code ""}.
     * @since 2.0
     */
    public static final String EMPTY = "";
    

    /**
     * Represents a failed index search.
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;
    
    
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean endsWith(final CharSequence str, final CharSequence suffix) {
		return endsWith(str, suffix, false);
	}

	private static boolean endsWith(final CharSequence str, final CharSequence suffix, final boolean ignoreCase) {
		if (str == null || suffix == null) {
			return str == null && suffix == null;
		}
		if (suffix.length() > str.length()) {
			return false;
		}
		final int strOffset = str.length() - suffix.length();
		return CharSequenceUtils.regionMatches(str, ignoreCase, strOffset, suffix, 0, suffix.length());
	}

	public static String capitalize(final String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}

		final char firstChar = str.charAt(0);
		if (Character.isTitleCase(firstChar)) {
			// already capitalized
			return str;
		}

		return new StringBuilder(strLen).append(Character.toTitleCase(firstChar)).append(str.substring(1)).toString();
	}

	public static String uncapitalize(final String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}

		final char firstChar = str.charAt(0);
		if (Character.isLowerCase(firstChar)) {
			// already uncapitalized
			return str;
		}

		return new StringBuilder(strLen).append(Character.toLowerCase(firstChar)).append(str.substring(1)).toString();
	}
	
	  public static String substring(final String str, int start) {
	        if (str == null) {
	            return null;
	        }

	        // handle negatives, which means last n characters
	        if (start < 0) {
	            start = str.length() + start; // remember start is negative
	        }

	        if (start < 0) {
	            start = 0;
	        }
	        if (start > str.length()) {
	            return EMPTY;
	        }

	        return str.substring(start);
	    }

	
	public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return EMPTY;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }
	
    public static int lastIndexOf(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        return CharSequenceUtils.lastIndexOf(seq, searchSeq, seq.length());
    }
    
    public static boolean endsWithIgnoreCase(final CharSequence str, final CharSequence suffix) {
        return endsWith(str, suffix, true);
    }
}
