package distributeblocks.util;

/**
 * Data container class for information about a transaction's
 * status when compared to all verified transactions. This
 * includes whether the transaction is a double spend and whether
 * all of the inputs it relies on are verified.
 *
 */
public class ValidationData {
	// by default, assume the best (not a double spend, and inputs are known)
	public boolean isDoubleSpend = false;
	public boolean inputsAreKnown = true;
}
