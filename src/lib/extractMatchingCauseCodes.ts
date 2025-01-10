import {causeCodes} from "@/lib/data/causeCodes";

export const extractMatchingCauseCodes = (
    causeCodeArray: Array<number> | undefined
): { label: string; value: number }[] | undefined => {

    return causeCodeArray?.map((code) => {
        const match = causeCodes.find((item) => item.value === code);
        return match
            ? { value: match.value, label: match.label }
            : { value: code, label: "" };
    });
}