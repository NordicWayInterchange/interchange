import {timeConverter} from "@/lib/timeConverter";

describe('timeConverter', () => {
  it('can convert to correct English date and time format', () => {
    Object.defineProperty(global, 'navigator', {
      value: { language: 'en-GB' },
    });

    const epochMillis = 1732619387 * 1000;
    const output = timeConverter(epochMillis);

    const [datePart, timePart] = output.split('\u2003');

    expect(datePart).toMatch(/^\d{1,2} \w{3} \d{4}$/);

    expect(timePart).toMatch(/^\d{2}:\d{2}:\d{2}$/);

    expect(output).toContain('\u2003');
  });
});
