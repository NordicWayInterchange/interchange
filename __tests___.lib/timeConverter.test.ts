import {timeConverter} from "@/lib/timeConverter";
describe("Can correctly convert Unix Epoch", () => {
  const epoch = 1732619387;
  const englishDateFormat = "26 Nov 2024 12:09:47";

  it("can convert to correct english date and time format", () => {
    jest.spyOn(navigator, 'language', 'get').mockReturnValue('en-GB');
    expect(timeConverter(epoch)).toBe(englishDateFormat);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });
});
