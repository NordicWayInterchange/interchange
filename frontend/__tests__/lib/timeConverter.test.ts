import { timeConverter } from "@/lib/timeConverter";
import { afterEach, describe, it, expect } from '@jest/globals';
import { jest } from '@jest/globals';

describe("Can correctly convert Unix Epoch", () => {
  const epoch = 1731527848;
  const englishDateFormat = "13 Nov 2024 20:57:28";
  const persianDateFormat = "۲۳ آبان ۱۴۰۳ ۲۰:۵۷:۲۸";

  it("can convert to correct english date and time format", () => {
    jest.spyOn(navigator, 'language', 'get').mockReturnValue('en-GB');
    expect(timeConverter(epoch)).toBe(englishDateFormat);
  });

  it("can convert to correct persian date and time format", () => {
    jest.spyOn(navigator, 'language', 'get').mockReturnValue('fa-IR');
    expect(timeConverter(epoch)).toBe(persianDateFormat);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });
});
