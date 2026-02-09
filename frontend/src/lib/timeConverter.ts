/**
 * @Description Converts Unix Epoch to a readable date and time
 * @param time - Unix Epoch
 * @returns {string} - Readable date and time
 */
export const timeConverter = (time: number): string => {
  const date = new Date(time * 1000);
  const locale = navigator.language|| 'en-GB';

  const localeDateString = date.toLocaleDateString(locale, {
    day: "2-digit",
    month: "short",
    year: "numeric"
  });

  const localeTimeString = date.toLocaleTimeString(locale, {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false
  });

  return `${localeDateString}\u2003${localeTimeString}`;
};
