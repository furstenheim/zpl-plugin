const fs = require('fs')
const data = fs.readFileSync('../resources/ZPLII-Prog.txt').toString().split('\n')

const UNDEFINED = 0
const CODE = 1
const DEFINITION = 2
const DESCRIPTION = 3
const PARAMETERS = 4

const mainDocument = data.slice(1282, 11668)
let isOddPage = false
let startPageIndex = 0
let currentCode
let status = UNDEFINED
const commands = []
for (let i = 0; i < mainDocument.length; i++){
  let line = mainDocument[i];
  if (line.startsWith('\f  ')) {
    isOddPage = true
    startPageIndex = i
    console.log('odd page', toIndex(i))
    continue
  }
  if (line.match(/^\f\d+/)) {
    isOddPage = false
    startPageIndex = i
    console.log('even page', toIndex(i))
    continue
  }
  if (status === CODE) {
    currentCode.definition = line
    status = DEFINITION
    continue
  }
  if (status === DEFINITION) {
    currentCode.description.push(line)
    status = DESCRIPTION
    continue
  }

  if (status === DESCRIPTION) {
    if (line.trim() === '') {
      status = UNDEFINED
    } else {
      currentCode.description.push(line)
    }
    continue
  }
  if (line.match(/^\s*Parameters\s*Details\s*/)) {
    status = PARAMETERS
    continue
  }
  if (status === PARAMETERS) {
    if (line.trim() === '') {
      status = UNDEFINED
    } else {
      currentCode.parameters.push(line)
    }
    continue
  }

  if (currentCode && line.trim().startsWith(`Format ${currentCode.code}`)) {
    currentCode.format.push(line)
  }
  let isCommand
  if (isOddPage) {
    isCommand = line.match(/^([~^]\w{1,3})\s*$/)
  } else {
    isCommand = line.match(/^ {5,6}([~^]\w{1,3})\s*$/)
  }
  if (isCommand && i === startPageIndex + 1) {
    console.log('Is command ignoring')
    continue
  }

  if (isCommand) {
    /*const previousLine = mainDocument[i - 1] || ''
    const nextLine = mainDocument[i + 1] || ''
    if (previousLine.trim().startsWith('^') || nextLine.trim().startsWith('^')) {
      console.log('Most probably an example, ignoring')
      continue
    }*/

    currentCode = {
      code: isCommand[1],
      definition: null,
      format: [],
      description: [],
      parameters: []
    }
    // console.log(currentCode, toIndex(i))
    commands.push(currentCode)
    status = CODE
  }
}
// console.log(commands)

const cleanedCommands = commands.map(function (c) {
  return {
    code: c.code,
    definition: c.definition.trim(),
    format: c.format.map(f => f.replace('Format', '').trim()),
    description: cleanDescription(c.description),
    parameters: cleanParameters(c.parameters)
  }
})

fs.writeFileSync('../src/main/resources/zpl.json', JSON.stringify(cleanedCommands, null, 2))
console.log(cleanedCommands)


function cleanDescription (d) {
  const padding = d[0].indexOf('D')
  const withoutPadding = d.map(line => line.substring(padding))
  withoutPadding[0] = withoutPadding[0].replace('Description ', '')
  return withoutPadding
}

/**
 *
 * @param {Array} p
 */
function cleanParameters (p) {
  let leftColumnIndex = Infinity
  let rightColumnIndex = Infinity
  const result = []
  for (let i = 0; i < p.length; i++){
    let line = p[i]
    const possibleParameter = line.match(/^(\s*)(\w|[<>#])+ = /)
    if (possibleParameter) {
      if (possibleParameter[1].length < rightColumnIndex) {
        console.log(rightColumnIndex, possibleParameter[1].length)
        leftColumnIndex = possibleParameter[1].length
        const subText = line.substring(leftColumnIndex)
        const match = subText.match(/(.*)(\s\s+)(.*)/)
        result.push(subText)

        if (match) {
          // Example         Note • This parameter is ignored on the R110Xi HF printer because
          const adjust = match[3].includes('Note') ? '        '.length : 0
          rightColumnIndex = leftColumnIndex + match[1].length + match[2].length - adjust
          console.log('~~~~4', line, match[3])
          continue
        }
      //  v = reverse the data order Reverses the data order
        const startIndex = mulIndexOf(subText, ['Accepted Values', 'Reverses the data order'])
        if (startIndex === -1) {
          console.error(line)
          throw new Error('Unexpected line')
        }
        rightColumnIndex = leftColumnIndex + startIndex

        continue
      }
    }

    if (!line.substr(0, leftColumnIndex).match(/\s*/)) {
      console.error(line, rightColumnIndex)
      throw new Error('Line does not match')
    }
    result.push(line.substring(leftColumnIndex))
  }
  return result
}

function mulIndexOf (string, values) {
  for (const value of values) {
    const index = string.indexOf(value)
    if (index !== -1) {
      return index
    }
  }
  return -1
}
/*console.log(commands)*/
function toIndex (i) {
  return i + 1283
}

function matchCommand (line) {
  return line.match(/^\s*([~^]\w{1,3})\s*$/)
}
