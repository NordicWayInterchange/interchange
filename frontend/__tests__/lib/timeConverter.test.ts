import { timeConverter } from "@/lib/timeConverter";
import { afterEach, beforeAll, describe, it, expect } from '@jest/globals';
import { jest } from '@jest/globals';

describe("Can correctly convert Unix Epoch", () => {

  beforeAll(() => {
    process.env.TZ = 'UTC';
  });

  const epoch = 1731527848;

  const englishDateFormat = "13 Nov 2024 19:57:28";
  const persianDateFormat = "۲۳ آبان ۱۴۰۳ ۱۹:۵۷:۲۸";

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