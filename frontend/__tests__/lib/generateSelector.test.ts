import { generateSelector } from "@/lib/generateSelector";
import { describe, it, expect } from '@jest/globals';

describe("Generate Selector", () => {
  const capability = {
    messageType: "DENM, IVIM",
    protocolVersion: "DENM:1.1.1",
    originatingCountry: "NO, SE",
    publisherId: "Bouvet-1, Bouvet-2",
    quadTree: ["1", "2", "3", "123"],
  };

  it("can generate selector", () => {
    const selector =
      "((messageType = 'DENM') OR (messageType = 'IVIM')) AND " +
      "(protocolVersion = 'DENM:1.1.1') AND " +
      "((originatingCountry = 'NO') OR (originatingCountry = 'SE')) AND " +
      "((publisherId = 'Bouvet-1') OR (publisherId = 'Bouvet-2')) AND " +
      "((quadTree like '%,1%') OR (quadTree like '%,2%') OR (quadTree like '%,3%') OR " +
      "(quadTree like '%,123%'))";
    expect(generateSelector(capability)).toBe(selector);
  });

  const capabilityWithAllExcludedKeys = {
    messageType: "SSEM",
    publisherId: "NO99999",
    originatingCountry: "FI",
    protocolVersion: "SSEM:0.0.2",
    quadTree: ["0"],
    redirect: "OPTIONAL",
    ids: ["1"],
    id: 1,
    iviType: ["1", "2"],
    stationTypes: ["3", "4"],
    publicationTypes: ["5", "6"],
  };

  it("can exclude elements", () => {
    const selector =
      "(messageType = 'SSEM') AND (publisherId = 'NO99999') AND " +
      "(originatingCountry = 'FI') AND (protocolVersion = 'SSEM:0.0.2') AND " +
      "(quadTree like '%,0%')";
    expect(generateSelector(capabilityWithAllExcludedKeys)).toBe(selector);
  });
});
