package com.itss.bldr.irdcl002.customer.creation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itss.l3.util2.L3LogHandler2;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;


/**
 * The Class CusMnemonicCheck.
 *
 * @author SURESH GANESAN
 * @version 1.0
 */

/*
 *******************************************************************
 * IRD: IRDCL001-IRDCL002 *
 ******************************************************************
 * Author: SURESH GANESAN ITSS
 * Purpose: Validate mnemonic
 * Attached:VERSION>CUSTOMER,ST.API.BLDR.CUS.CREATION.1.0.0
 * Date : 08/Apr/2021
 *******************************************************************
 * 
 * ------------------------------------------------------------------
 * Revision History:
 * ------------------------------------------------------------------
 * Date :30/Aug/2021
 * Author: Suresh Ganesan ITSS
 * Description: TSR-142406
 * Mnemonic validation - change due to MIGRATION agreement with bank
 * ******************************************************************
 * Date : 16-Sep-2021
 * Author: SURESH GANESAN ITSS 
 * Description: Recompiled for R21 upgrade 
 * ******************************************************************
 * Date : 28-Dec-2021
 * Author: SURESH GANESAN ITSS 
 * Description: log4j2 vulnerability 
 * ******************************************************************
 */

public class CusMnemonicCheck extends RecordLifecycle {

    /** The Constant LG. */
    private static final Logger LG = LogManager.getLogger(CusMnemonicCheck.class.getName());

    /** The Constant EB_ERROR_2. */
    private static final String EB_ERROR_2 = "AC-INP.MAND";

    /** The Constant EB_ERROR_3. */
    private static final String EB_ERROR_3 = "EB-MNEMONIC.PREFIX";

    /** The Constant EB_ERROR_4. */
    private static final String EB_ERROR_4 = "EB-MNEMONIC.INVALID";

    /**
     * Log process.
     *
     * @param application the application
     * @param currentRecordId the current record id
     * @param currentRecord the current record
     * @param unauthorisedRecord the unauthorised record
     * @param liveRecord the live record
     * @param transactionContext the transaction context
     * @return the t validation response
     */
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        logProcess();
        LG.debug(transactionContext);
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        CustomerRecord cusRecInau = new CustomerRecord(unauthorisedRecord);
        LG.debug(cusRecInau);
        CustomerRecord cusRecLive = new CustomerRecord(liveRecord);
        LG.debug(cusRecLive);
        TField tMnemonic = cusRec.getMnemonic();
        int yLength = tMnemonic.getValue().length();
        // Length Check - Min 3 ; Max 10;TSR-142406
        if ((yLength > 10) || (yLength < 3)) {
            tMnemonic.setError(EB_ERROR_4);// Mnemonic Should not be greater than 10; Invalid Mnemonic
            LG.error("Mnemonic greater than 10 - agreed length ");
            return cusRec.getValidationResponse();
        }
        // Numeric check
        String regexCondition = "[0-9]+";
        Pattern p = Pattern.compile(regexCondition);
        String mnemonicRest = tMnemonic.getValue().substring(1);
        Matcher m = p.matcher(mnemonicRest);
        Boolean numericFlag = m.matches();
        if (Boolean.FALSE.equals(numericFlag)) {
            tMnemonic.setError(EB_ERROR_4); // Invalid Mnemonic
            LG.error("Invalid Mnemonic");
            return cusRec.getValidationResponse();
        }

        // Type check - C / O
        String yTrimmedValue = tMnemonic.getValue().substring(0, 1);
        String yGender = cusRec.getGender().getValue();
        int yGenderLength = yGender.length();
        switch (yTrimmedValue) {
            case "C":// Natural customer
                if (cusRec.getName2().isEmpty()) {
                    TField name2s = new TField();
                    cusRec.setName2(name2s, 0);
                    name2s.setError(EB_ERROR_2);// INPUT MANDATORY
                    LG.error("INPUT MANDATORY");
                }
                if (yGenderLength <= 0) {
                    cusRec.getGender().setError(EB_ERROR_2);// INPUT MANDATORY
                    LG.error("INPUT MANDATORY");
                }
                break;
            case "O":// Corporate customer- Do nothing

                break;
            default:
                tMnemonic.setError(EB_ERROR_3);// Prefix should be either C or O
                LG.error("Prefix should be either C or O");
        }
        return cusRec.getValidationResponse();
    }

    /**
     * Log process.
     */
    public static void logProcess() {
        L3LogHandler2.logHandler();

    }

}
