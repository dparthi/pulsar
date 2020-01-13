counter = 0

request = function()
  counter = counter + 1
  return wrk.format(nil, "counter")
end
