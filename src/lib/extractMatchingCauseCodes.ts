import {causeCodes} from "@/lib/data/causeCodes";

export const extractMatchingCauseCodes = (
    causeCodeArray: Array<number> | undefined
): any[] => {

    const matchingData = causeCodes.filter((item) => causeCodeArray?.includes(item.value));

    const result = matchingData.map((item) => ({
        value: item.value,
        label: item.label,
    }));

    if (result.length > 0) {
        return result;
    } else {
        return [];
    }
}