import { escapeString } from "@/lib/escapeString";
import { describe, it, expect } from '@jest/globals';

describe("Can correctly escape string", () => {
  const text = "Thi$ i$ @n %llegal te3t!!+?";
  const escapedText = "Thi__i__@n__llegal_te3t____";

  it("can escape a text with illegal characters", () => {
    expect(escapeString(text)).toBe(escapedText);
  });
});
