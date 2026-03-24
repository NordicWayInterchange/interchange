import { handleDecoding } from "@/lib/handleFile";
import { describe, it, expect } from '@jest/globals';

describe("Can correctly decode Base64", () => {
  const encoded = "VGVzdCBmb3IgQmFzZTY0IGRlY29kaW5n";
  const decoded = "Test for Base64 decoding";

  it("can decode single string", () => {
    expect(handleDecoding(encoded)).toBe(decoded);
  });

  const encodedArray = [
    "VGVzdCBmb3IgQmFzZTY0IGRlY29kaW5nIDE=",
    "VGVzdCBmb3IgQmFzZTY0IGRlY29kaW5nIDI=",
  ];

  const decodedArray = "Test for Base64 decoding 1Test for Base64 decoding 2";

  it("can decode string[]", () => {
    expect(handleDecoding(encodedArray)).toBe(decodedArray);
  });
});
