package ru.rb.ccdea.storage.persistence.details;

import java.util.Date;

public class DossierKeyDetails {
    public String dossierId = null;
    public String branchCode = null;
    public String customerNumber = null;
    public String customerName = null;
    public String passportNumber = null;
    public String contractNumber = null;
    public Date contractDate = null;

    @Override
    public String toString() {
        return "BranchCode: " + branchCode +
                ",CustomerNumber:" + customerNumber +
                ",PassportNumber:" + passportNumber +
                ",ContractNumber:" + contractNumber +
                ",ContractDate:" + contractDate;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = true;
        if (obj instanceof DossierKeyDetails) {
            DossierKeyDetails dossierKeyDetails = (DossierKeyDetails)obj;
            if (!branchCode.equals(dossierKeyDetails.branchCode)) {
                result = false;
            }
            else if (!customerNumber.equals(dossierKeyDetails.customerNumber)) {
                result = false;
            }
            else {
                if (passportNumber != null) {
                    result = passportNumber.equals(dossierKeyDetails.passportNumber);
                }
                else {
                    if (contractNumber != null) {
                        if (!contractNumber.equals(dossierKeyDetails.contractNumber)) {
                            result = false;
                        }
                    }
                    else if (dossierKeyDetails.contractNumber != null) {
                        result = false;
                    }
                    if (contractDate != null) {
                        if (!contractDate.equals(dossierKeyDetails.contractDate)) {
                            result = false;
                        }
                    }
                    else if (dossierKeyDetails.contractDate != null){
                        result = false;
                    }
                }
            }
        }
        else {
            result = super.equals(obj);
        }
        return result;
    }

	public String getDossierId() {
		return dossierId;
	}

	public void setDossierId(String dossierId) {
		this.dossierId = dossierId;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getPassportNumber() {
		return passportNumber;
	}

	public void setPassportNumber(String passportNumber) {
		this.passportNumber = passportNumber;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}
}
