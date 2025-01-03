import {timeConverter} from "@/lib/timeConverter";
describe("Can correctly convert Unix Epoch", () => {
  const epoch = 1732619387;
  const englishDateFormat = "26 Nov 2024 12:09:47";

  it("can convert to correct English date and time format", () => {
    Object.defineProperty(navigator, 'language', {
      get: jest.fn().mockReturnValue('en-GB'),
    });

    expect(timeConverter(epoch)).toEqual(englishDateFormat);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });
});
