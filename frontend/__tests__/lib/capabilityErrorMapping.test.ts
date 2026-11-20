import {
  CAPABILITY_FIELD_NAMES,
  formatCapabilityError,
  mapErrorMessageToField,
} from "@/lib/capabilityErrorMapping";
import { describe, it, expect } from "@jest/globals";

describe("mapErrorMessageToField", () => {
  it("maps a publicationId error message to the publicationId field", () => {
    expect(
      mapErrorMessageToField("publicationId must start with '<publisherId>:'")
    ).toBe("publicationId");
  });

  it("maps a publisherId error message to the publisherId field", () => {
    expect(
      mapErrorMessageToField(
        "publisherId must contain exactly two uppercase letters followed by five digits in the format <country code><5 numbers>"
      )
    ).toBe("publisherId");
  });

  it("maps an originatingCountry error message to the originatingCountry field", () => {
    expect(mapErrorMessageToField("'no' is not a valid country code")).toBeUndefined();
    expect(
      mapErrorMessageToField("originatingCountry contains illegal characters")
    ).toBe("originatingCountry");
  });

  it("returns undefined when no known field matches", () => {
    expect(mapErrorMessageToField("Something went terribly wrong")).toBeUndefined();
  });

  it("only contains known capability form field names", () => {
    expect(CAPABILITY_FIELD_NAMES).toEqual(
      expect.arrayContaining([
        "publisherId",
        "publicationId",
        "originatingCountry",
        "protocolVersion",
        "quadTree",
        "publisherName",
        "publicationType",
        "causeCode",
        "messageType",
      ])
    );
  });
});

describe("formatCapabilityError", () => {
  it("formats the error code and message into a readable string", () => {
    const formatted = formatCapabilityError({
      code: "INVALID_PUBLICATION_ID_PREFIX",
      message: "publicationId must start with '<publisherId>:'",
    });

    expect(formatted).toBe(
      "INVALID_PUBLICATION_ID_PREFIX: publicationId must start with '<publisherId>:'"
    );
  });
});


