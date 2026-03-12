/**
 * @Description Parse object to selector
 * @param formState - Form object
 * @returns {string} - JMS Message Selector
 */
export const generateSelector = (formState: any): string => {
  const excludeKeys = [
    "redirect",
    "id",
    "ids",
    "publicationTypes",
    "stationTypes",
    "iviType",
  ];

  return Object.keys(formState).reduce(
    (acc: string, key: string, index: number) => {
      const value = formState[key as keyof typeof formState];

      const valueArray = Array.isArray(value)
        ? value
        : value.toString().split(",").filter(Boolean);

      if (!excludeKeys.includes(key)) {
        if (valueArray.length > 0) {
          if (index && acc) {
            acc += " AND ";
          }
          const reduce = valueArray.reduce(
            (acc: string, value: string, index: number) =>
              reducer(acc, value.toString().trim(), index, key),
            ""
          );
          acc += valueArray.length > 1 ? `(${reduce})` : reduce;
        }
      }

      return acc;
    },
    ""
  );
};

const reducer = (
  acc: string,
  value: string,
  index: number,
  key: string
): string => {
  if (value) {
    if (index != 0) {
      acc += " OR ";
    }
    switch (key) {
      case "quadTree":
        acc += `(${key} like '%,${value}%')`;
        break;
      case "causeCode":
        acc += `(${key} = ${value})`;
        break;
      default:
        acc += `(${key} = '${value}')`;
    }
  }
  return acc;
};
