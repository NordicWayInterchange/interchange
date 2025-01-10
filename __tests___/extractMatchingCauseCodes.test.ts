import {extractMatchingCauseCodes} from "@/lib/extractMatchingCauseCodes";

describe('extractMatchingCauseCodes', () => {
    it('should return matching value and label', () => {
        const causeCodeArray = [1, 99];
        const result = extractMatchingCauseCodes(causeCodeArray);

        expect(result).toEqual([
            {value: 1, label: "Traffic"},
            {value: 99, label: "Dangerous situation"},
        ]);
    });

    it('should return the value with empty label', () => {
        const causeCodeArray = [5];
        const result = extractMatchingCauseCodes(causeCodeArray);

        expect(result).toEqual([
            {value: 5, label: ""},
        ]);
    });
});
