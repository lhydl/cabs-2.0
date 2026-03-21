const { pathsToModuleNameMapper } = require('ts-jest');

const {
  compilerOptions: { paths = {}, baseUrl = './' },
} = require('./tsconfig.json');
const environment = require('./webpack/environment');

module.exports = {
  transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$|dayjs/esm)'],
  resolver: 'jest-preset-angular/build/resolvers/ng-jest-resolver.js',
  globals: {
    ...environment,
  },
  roots: ['<rootDir>', `<rootDir>/${baseUrl}`],
  modulePaths: [`<rootDir>/${baseUrl}`],
  setupFiles: ['jest-date-mock'],
  cacheDirectory: '<rootDir>/target/jest-cache',
  coverageDirectory: '<rootDir>/target/test-results/',
  moduleNameMapper: pathsToModuleNameMapper(paths, { prefix: `<rootDir>/${baseUrl}/` }),
  reporters: [
    'default',
    ['jest-junit', { outputDirectory: '<rootDir>/target/test-results/', outputName: 'TESTS-results-jest.xml' }],
    ['jest-sonar', { outputDirectory: './target/test-results/jest', outputName: 'TESTS-results-sonar.xml' }],
  ],
  testMatch: ['<rootDir>/src/app/**/@(*.)@(spec.ts)'],
  testEnvironmentOptions: {
    url: 'https://jhipster.tech',
  },
  testPathIgnorePatterns: [
    // patient entity not used
    '<rootDir>/src/app/entities/patient/list/patient.component.spec.ts',
    '<rootDir>/src/app/entities/patient/delete/patient-delete-dialog.component.spec.ts',
    '<rootDir>/src/app/entities/patient/detail/patient-detail.component.spec.ts',
    '<rootDir>/src/app/entities/patient/route/patient-routing-resolve.service.spec.ts',
    '<rootDir>/src/app/entities/patient/service/patient.service.spec.ts',
    '<rootDir>/src/app/entities/patient/update/patient-update.component.spec.ts',
    '<rootDir>/src/app/entities/patient/update/patient-form.service.spec.ts',
    // page-ribbon not used
    '<rootDir>/src/app/layouts/profiles/page-ribbon.component.spec.ts',

    '<rootDir>/src/app/entities/appointment/update/appointment-form.service.spec.ts',
    '<rootDir>/src/app/entities/appointment/detail/appointment-detail.component.spec.ts',
  ],
};
