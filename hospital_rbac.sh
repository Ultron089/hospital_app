#!/bin/bash

# ================================================================
# Hospital Medical Records - Custom RBAC Wrapper
# Usage: ./hospital_rbac.sh <username> <action>
# ================================================================

RECORD_FILE="$HOME/hospital_app/records/patient_data.log"

# Require exactly two arguments
if [ "$#" -ne 2 ]; then
    echo "AUDIT LOG | Identity: UNKNOWN | Role: UNKNOWN | Action: INVALID | Status: Failure"
    echo "Usage: $0 <username> <action>"
    exit 1
fi

USER_IDENTITY="$1"
REQUESTED_ACTION="$2"

# ------------------------------------------------
# Identity-to-Role Mapping
# ------------------------------------------------
case "$USER_IDENTITY" in
    "smith")
        ROLE="Medical_Staff"
        ;;
    "jones")
        ROLE="Compliance_Officer"
        ;;
    "taylor")
        ROLE="General_Staff"
        ;;
    *)
        echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: UNMAPPED | Action: $REQUESTED_ACTION | Status: Failure"
        echo "ACCESS DENIED: Unknown or unmapped identity."
        exit 1
        ;;
esac

# ------------------------------------------------
# Permission Enforcement
# ------------------------------------------------
case "$ROLE" in
    "Medical_Staff")
        if [ "$REQUESTED_ACTION" == "view" ]; then
            echo "Patient Records:"
            cat "$RECORD_FILE"
            echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Success"
        elif [ "$REQUESTED_ACTION" == "append" ]; then
            echo "Enter diagnostic log message:"
            read -r MESSAGE
            echo "$MESSAGE" >> "$RECORD_FILE"
            echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Success"
        else
            echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Failure"
            echo "ACCESS DENIED: Invalid or unauthorized action."
            exit 1
        fi
        ;;

    "Compliance_Officer")
        if [ "$REQUESTED_ACTION" == "view" ]; then
            echo "Patient Records:"
            cat "$RECORD_FILE"
            echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Success"
        elif [ "$REQUESTED_ACTION" == "append" ]; then
            echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Failure"
            echo "ACCESS DENIED: Compliance_Officer is not authorized to append diagnostic logs."
            exit 1
        else
            echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Failure"
            echo "ACCESS DENIED: Invalid or unauthorized action."
            exit 1
        fi
        ;;

    "General_Staff")
        echo "AUDIT LOG | Identity: $USER_IDENTITY | Role: $ROLE | Action: $REQUESTED_ACTION | Status: Failure"
        echo "ACCESS DENIED: General_Staff has no authorized actions."
        exit 1
        ;;
esac
