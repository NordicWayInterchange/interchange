import {timeConverter} from "@/lib/timeConverter";
describe('timeConverter', () => {
  it('can convert to correct English date and time format', () => {
    Object.defineProperty(global, 'navigator', {
      value: {
        language: 'en-GB',
      },
    });

    const epochSeconds = 1732619387;
    const epochMillis = epochSeconds * 1000;
    const expectedDate = "26 Nov 2024";
    const expectedTime = "12:09:47";
    const expectedFormat = `${expectedDate}\u2003${expectedTime}`;

    expect(timeConverter(epochMillis)).toEqual(expectedFormat);
  });
});
