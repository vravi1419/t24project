package com.itss.bldr.irdcl002.customer.creation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itss.l3.util2.L3LogHandler2;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.customer.LegalIdClass;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebcuslegalid.EbCusLegalIdRecord;

/**
 * The Class CusLegalCheck.
 *
 * @author Suresh Ganesan,ITSS
 */

/*
 *******************************************************************
 * IRD: IRDCL001-IRDCL002 *
 ******************************************************************
 * Author:SURESH GANESAN ITSS
 * Purpose:Validate Legal id
 * Attached:VERSION>CUSTOMER,ST.API.BLDR.CUS.CREATION.1.0.0
 * Date : 28/Mar/2021
 *******************************************************************
 * 
 * ------------------------------------------------------------------
 * Revision History:
 * ------------------------------------------------------------------
 * Date : 10-Sep-2021
 * Author: SURESH GANESAN ITSS 
 * Description: Recompiled for core updates .jar - TSR-142406
 * ******************************************************************
 * Date : 16-Sep-2021
 * Author: SURESH GANESAN ITSS 
 * Description: Recompiled for R21 upgrade 
 * ******************************************************************
 * Date : 28-Dec-2021
 * Author: SURESH GANESAN ITSS 
 * Description: log4j2 vulnerability 
 * ******************************************************************
 * Date : 30-Jan-2022
 * Author: SURESH GANESAN ITSS 
 * Description: TSR-239389-legal doc duplicate 
 * ******************************************************************
 */

public class CusLegalCheck extends RecordLifecycle {

    /** The Constant LEGAL_CONCAT_TABLE. */
    private static final String LEGAL_CONCAT_TABLE = "CUS.LEGAL.ID";

    /** The Constant EB_ERROR_ID1. */
    private static final String EB_ERROR_ID1 = "ST-BHDL.LEGAL.DUPLICATE";

    /** The da. */
    DataAccess da = new DataAccess(this);

    /** The Constant Y_SEP. */
    private static final String Y_SEP = "-";

    /** The current record. */
    TStructure currentRecord;

    /** The Constant LG. */
    private static final Logger LG = LogManager.getLogger(CusLegalCheck.class.getName());

    List<String> legalDocnameListCheck = new ArrayList<>();
    List<LegalIdClass> legalIdMain;
    private static final String EB_ERROR_ID2 = "ST-BHDL.LEGAL.DOC.DUP";

    /**
     * Validate record.
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
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        if (transactionContext.getCurrentOperation().equalsIgnoreCase("VALIDATE")) {
            LG.info("Return for validate process");
            return cusRec.getValidationResponse();
        }
        LG.debug(application);
        LG.debug(currentRecordId);
        LG.debug(currentRecord);
        LG.debug(unauthorisedRecord);
        LG.debug(liveRecord);
        LG.debug(transactionContext);
        this.currentRecord = currentRecord;
        legalIdMain = cusRec.getLegalId();
        int totSize = legalIdMain.size();
        for (int i = 0; i < totSize; i++) {
            try {
                String legalId = legalIdMain.get(i).getLegalId().getValue();
                String legalDocName = legalIdMain.get(i).getLegalDocName().getValue();
                legalDocnameListCheck.add(legalDocName);
                String legalIdDocName = legalId + Y_SEP + legalDocName;
                TStructure recConcat = da.getRecord(LEGAL_CONCAT_TABLE, legalIdDocName);
                if (recConcat != null) {
                    EbCusLegalIdRecord cusLegalIdRec = new EbCusLegalIdRecord(recConcat);
                    List<TField> idsArray = cusLegalIdRec.getCusLegalId();
                    for (TField id : idsArray) {
                        String loopId = id.toString();
                        if ((!loopId.equals(currentRecordId)))
                        // whole match equal but current id should not be
                        // considered
                        {
                            LG.error("Duplicate found in {} -", loopId);
                            legalIdMain.get(i).getLegalId().setError(EB_ERROR_ID1);
                        }
                    }
                }

            } catch (T24CoreException ignored) {
                LG.info("Legal id not duplicate for this customer-{}", currentRecordId);
            }
        }
        Set<String> setCount = new HashSet<>(legalDocnameListCheck);
        if (!legalDocnameListCheck.isEmpty() && setCount.size() < legalDocnameListCheck.size()) {
            legalIdMain.get(0).getLegalId().setError(EB_ERROR_ID2);
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
