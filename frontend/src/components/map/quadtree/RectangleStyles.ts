const rectangleStyle = {
  color: "#ff0000",
  weight: 1,
  opacity: 0.3,
  fillOpacity: 0,
  fill: true,
  lineCap: "butt",
};

const rectangleStyleHover = {
  ...rectangleStyle,
  color: "#00ff00",
  fillOpacity: 0.1,
};

const rectangleStyleSelect = {
  ...rectangleStyle,
  color: "#0000ff",
  fillOpacity: 0.1,
};

export { rectangleStyle, rectangleStyleHover, rectangleStyleSelect };
